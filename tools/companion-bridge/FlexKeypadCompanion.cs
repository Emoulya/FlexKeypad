using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Forms;
using Microsoft.Win32;

namespace FlexKeypad.Companion
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            string logPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "companion_run.log");
            AppDomain.CurrentDomain.UnhandledException += (s, e) =>
            {
                File.AppendAllText(logPath, "UnhandledException: " + e.ExceptionObject.ToString() + "\n");
            };
            Application.ThreadException += (s, e) =>
            {
                File.AppendAllText(logPath, "ThreadException: " + e.Exception.ToString() + "\n");
            };
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);

            try
            {
                bool createdNew;
                using (Mutex mutex = new Mutex(true, "Global\\FlexKeypadCompanionMutex", out createdNew))
                {
                    File.AppendAllText(logPath, "Main: createdNew=" + createdNew + "\n");
                    if (!createdNew)
                    {
                        File.AppendAllText(logPath, "Already running, exiting.\n");
                        return;
                    }

                    Application.EnableVisualStyles();
                    Application.SetCompatibleTextRenderingDefault(false);
                    File.AppendAllText(logPath, "Starting Application.Run...\n");
                    Application.Run(new CompanionAppContext());
                    File.AppendAllText(logPath, "Application.Run exited cleanly.\n");
                }
            }
            catch (Exception ex)
            {
                try
                {
                    string path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "companion_error.log");
                    File.WriteAllText(path, ex.ToString());
                }
                catch {}
            }
        }
    }

    public class CompanionAppContext : ApplicationContext
    {
        private NotifyIcon notifyIcon;
        private ContextMenuStrip contextMenu;
        private ToolStripMenuItem statusMenuItem;
        private ToolStripMenuItem startupMenuItem;
        private Icon iconConnected;
        private Icon iconDisconnected;

        private Thread watchdogThread;
        private volatile bool isRunning = true;
        private volatile bool isConnected = false;
        private TcpClient currentClient;
        private SynchronizationContext uiContext;

        private const int PORT = 8899;
        private const string RUN_REG_KEY = @"Software\Microsoft\Windows\CurrentVersion\Run";
        private const string APP_NAME = "FlexKeypadCompanion";

        private static void Log(string msg)
        {
            try
            {
                string p = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "companion_run.log");
                File.AppendAllText(p, DateTime.Now.ToString("HH:mm:ss.fff") + " " + msg + "\n");
            }
            catch {}
        }

        public CompanionAppContext()
        {
            Log("CompanionAppContext constructor started");
            uiContext = SynchronizationContext.Current ?? new WindowsFormsSynchronizationContext();

            iconConnected = CreateStatusIcon(Color.FromArgb(0, 230, 118)); // Vibrant Neon Green
            Log("iconConnected created");
            iconDisconnected = CreateStatusIcon(Color.FromArgb(120, 144, 156)); // Muted Blue Gray
            Log("iconDisconnected created");

            InitializeTray();
            Log("Tray initialized");
            EnsureAutoStartup();
            RealDesktopInput.Start();
            Log("RealDesktopInput started");

            watchdogThread = new Thread(WatchdogLoop);
            watchdogThread.IsBackground = true;
            watchdogThread.Start();
            Log("Watchdog started");
        }

        private void InitializeTray()
        {
            contextMenu = new ContextMenuStrip();

            ToolStripMenuItem titleItem = new ToolStripMenuItem("FlexKeypad Companion v1.0");
            titleItem.Enabled = false;
            titleItem.Font = new Font(contextMenu.Font, FontStyle.Bold);
            contextMenu.Items.Add(titleItem);

            contextMenu.Items.Add(new ToolStripSeparator());

            statusMenuItem = new ToolStripMenuItem("Status: Waiting for USB...");
            statusMenuItem.Enabled = false;
            contextMenu.Items.Add(statusMenuItem);

            ToolStripMenuItem reconnectItem = new ToolStripMenuItem("Reconnect Now", null, OnReconnectClicked);
            contextMenu.Items.Add(reconnectItem);

            startupMenuItem = new ToolStripMenuItem("Run on Windows Startup", null, OnStartupToggled);
            startupMenuItem.Checked = IsStartupEnabled();
            contextMenu.Items.Add(startupMenuItem);

            contextMenu.Items.Add(new ToolStripSeparator());

            ToolStripMenuItem exitItem = new ToolStripMenuItem("Exit", null, OnExitClicked);
            contextMenu.Items.Add(exitItem);

            notifyIcon = new NotifyIcon();
            notifyIcon.Icon = iconDisconnected;
            notifyIcon.Text = "FlexKeypad: Waiting for USB device...";
            notifyIcon.ContextMenuStrip = contextMenu;
            notifyIcon.Visible = true;

            notifyIcon.DoubleClick += (s, e) =>
            {
                string msg = isConnected 
                    ? "FlexKeypad is CONNECTED via USB!\nKeystrokes are active." 
                    : "FlexKeypad is waiting for USB connection.\nPlug in your phone and open FlexKeypad.";
                notifyIcon.ShowBalloonTip(3000, "FlexKeypad Status", msg, ToolTipIcon.Info);
            };
        }

        private Icon CreateStatusIcon(Color accentColor)
        {
            using (Bitmap bmp = new Bitmap(32, 32))
            using (Graphics g = Graphics.FromImage(bmp))
            {
                g.SmoothingMode = SmoothingMode.AntiAlias;
                g.Clear(Color.Transparent);

                // Background pill / circle
                using (Brush bgBrush = new SolidBrush(Color.FromArgb(24, 28, 36)))
                {
                    g.FillEllipse(bgBrush, 2, 2, 28, 28);
                }

                // Accent ring / dot
                using (Pen pen = new Pen(accentColor, 3f))
                {
                    g.DrawEllipse(pen, 4, 4, 24, 24);
                }

                // Inner core dot
                using (Brush coreBrush = new SolidBrush(accentColor))
                {
                    g.FillEllipse(coreBrush, 11, 11, 10, 10);
                }

                IntPtr hIcon = bmp.GetHicon();
                return Icon.FromHandle(hIcon);
            }
        }

        private void SetConnectionState(bool connected)
        {
            if (isConnected == connected) return;
            isConnected = connected;
            if (notifyIcon == null) return;

            if (connected)
            {
                notifyIcon.Icon = iconConnected;
                notifyIcon.Text = "FlexKeypad: Connected (USB)";
                statusMenuItem.Text = "Status: Connected (USB Active)";
                statusMenuItem.ForeColor = Color.DarkGreen;
                notifyIcon.ShowBalloonTip(2500, "FlexKeypad Connected", "Device connected via USB! Keystrokes are active.", ToolTipIcon.Info);
            }
            else
            {
                notifyIcon.Icon = iconDisconnected;
                notifyIcon.Text = "FlexKeypad: Waiting for USB device...";
                statusMenuItem.Text = "Status: Waiting for USB...";
                statusMenuItem.ForeColor = Color.Gray;
            }
        }

        private void WatchdogLoop()
        {
            Log("WatchdogLoop entered");
            while (isRunning)
            {
                try
                {
                    // Fast check: Try connecting directly to localhost:8899
                    bool connected = TryConnectAndStream();

                    if (!connected)
                    {
                        // Ensure adb forward is active
                        RunSilentAdbForward();
                    }

                    // Always sleep at least 1500ms to throttle retries and prevent socket spam
                    Thread.Sleep(1500);
                }
                catch (Exception ex)
                {
                    Log("WatchdogLoop exception: " + ex.Message);
                    Thread.Sleep(2000);
                }
            }
        }

        private bool TryConnectAndStream()
        {
            TcpClient client = null;
            try
            {
                client = new TcpClient();
                currentClient = client;

                IAsyncResult ar = client.BeginConnect(IPAddress.Loopback, PORT, null, null);
                bool waitSuccess = ar.AsyncWaitHandle.WaitOne(1000);

                if (!waitSuccess || !client.Connected)
                {
                    return false;
                }

                client.EndConnect(ar);
                client.NoDelay = true;

                NetworkStream stream = client.GetStream();
                stream.ReadTimeout = 2500; // Handshake timeout
                StreamWriter writer = new StreamWriter(stream, Encoding.UTF8) { AutoFlush = true };
                StreamReader reader = new StreamReader(stream, Encoding.UTF8);

                // Send initial handshake so the bidirectional tunnel stays active
                writer.WriteLine("{\"type\":\"CONNECT\",\"version\":1}");

                // Await handshake ACK from Android app
                string firstLine = null;
                try
                {
                    firstLine = reader.ReadLine();
                }
                catch (Exception)
                {
                    return false;
                }

                if (string.IsNullOrEmpty(firstLine) || !firstLine.Contains("CONNECTED_ACK"))
                {
                    Log("Handshake not acknowledged by device: " + (firstLine ?? "<null>"));
                    return false;
                }

                Log("Watchdog: Handshake successful with FlexKeypad at port 8899!");
                stream.ReadTimeout = 3500; // 3.5s timeout: auto-detects disconnect when no PONG or event received
                UpdateUiSafe(delegate() { SetConnectionState(true); });

                // Background heartbeat ping thread (sends PING every 1.5s)
                Thread pingThread = new Thread(delegate()
                {
                    while (isRunning && client.Connected)
                    {
                        try
                        {
                            Thread.Sleep(1500);
                            if (client.Connected)
                            {
                                writer.WriteLine("{\"type\":\"PING\"}");
                            }
                        }
                        catch { break; }
                    }
                });
                pingThread.IsBackground = true;
                pingThread.Start();

                while (isRunning && client.Connected)
                {
                    string line = reader.ReadLine();
                    if (line == null) break; // End of stream / disconnected
                    line = line.Trim();
                    if (line.Length > 0)
                    {
                        ProcessJsonEvent(line);
                    }
                }

                Log("Watchdog: Disconnected from FlexKeypad.");
                return true;
            }
            catch (Exception ex)
            {
                Log("TryConnectAndStream error: " + ex.Message);
                return false;
            }
            finally
            {
                currentClient = null;
                UpdateUiSafe(delegate() { SetConnectionState(false); });
                if (client != null)
                {
                    try { client.Close(); } catch {}
                }
            }
        }

        private static string cachedAdbPath = null;
        private static string FindAdbPath()
        {
            if (cachedAdbPath != null) return cachedAdbPath;

            string localApp = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string sdkAdb = Path.Combine(localApp, @"Android\Sdk\platform-tools\adb.exe");
            if (File.Exists(sdkAdb))
            {
                cachedAdbPath = sdkAdb;
                return cachedAdbPath;
            }

            string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86);
            string altAdb = Path.Combine(programFiles, @"Android\android-sdk\platform-tools\adb.exe");
            if (File.Exists(altAdb))
            {
                cachedAdbPath = altAdb;
                return cachedAdbPath;
            }

            cachedAdbPath = "adb.exe";
            return cachedAdbPath;
        }

        private static bool RunSilentAdbForward()
        {
            try
            {
                string adbExe = FindAdbPath();
                ProcessStartInfo psi = new ProcessStartInfo(adbExe, "forward tcp:8899 tcp:8899");
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
                psi.WindowStyle = ProcessWindowStyle.Hidden;
                psi.RedirectStandardOutput = true;
                psi.RedirectStandardError = true;
                using (Process p = Process.Start(psi))
                {
                    if (p != null)
                    {
                        p.BeginOutputReadLine();
                        p.BeginErrorReadLine();
                        bool finished = p.WaitForExit(3000);
                        if (!finished)
                        {
                            try { p.Kill(); } catch {}
                        }
                        Log("RunSilentAdbForward: finished=" + finished + ", exitCode=" + (finished ? p.ExitCode.ToString() : "timeout"));
                        return finished && p.ExitCode == 0;
                    }
                }
            }
            catch (Exception ex)
            {
                Log("RunSilentAdbForward error: " + ex.ToString());
            }
            return false;
        }

        private void ProcessJsonEvent(string json)
        {
            try
            {
                // Parse action: "DOWN", "UP", "RELEASE_ALL", "CONNECTED_ACK", "PING"
                Match actionMatch = Regex.Match(json, "\"action\"\\s*:\\s*\"([^\"]+)\"");
                if (!actionMatch.Success) return;
                string action = actionMatch.Groups[1].Value;

                if (action == "CONNECTED_ACK" || action == "PING" || action == "PONG")
                {
                    return;
                }

                if (action == "RELEASE_ALL")
                {
                    RealDesktopInput.ReleaseAllModifiers();
                    return;
                }

                // Parse keyCode
                Match keyMatch = Regex.Match(json, "\"keyCode\"\\s*:\\s*(\\d+)");
                int hidCode = keyMatch.Success ? int.Parse(keyMatch.Groups[1].Value) : 0;

                // Parse modifiers
                List<string> modifiers = new List<string>();
                Match modMatch = Regex.Match(json, "\"modifiers\"\\s*:\\s*\\[([^\\]]*)\\]");
                if (modMatch.Success)
                {
                    string inner = modMatch.Groups[1].Value;
                    MatchCollection mc = Regex.Matches(inner, "\"([^\"]+)\"");
                    foreach (Match m in mc)
                    {
                        modifiers.Add(m.Groups[1].Value);
                    }
                }

                ushort vk = HidToVkMapper.GetVkCode(hidCode);

                if (action == "DOWN")
                {
                    // Apply modifiers
                    if (modifiers.Contains("MODIFIER_LEFT_CTRL") || modifiers.Contains("MODIFIER_RIGHT_CTRL"))
                        RealDesktopInput.SendKey(0x11, true); // VK_CONTROL
                    if (modifiers.Contains("MODIFIER_LEFT_ALT") || modifiers.Contains("MODIFIER_RIGHT_ALT"))
                        RealDesktopInput.SendKey(0x12, true); // VK_MENU
                    if (modifiers.Contains("MODIFIER_LEFT_SHIFT") || modifiers.Contains("MODIFIER_RIGHT_SHIFT"))
                        RealDesktopInput.SendKey(0x10, true); // VK_SHIFT
                    if (modifiers.Contains("MODIFIER_LEFT_GUI") || modifiers.Contains("MODIFIER_RIGHT_GUI"))
                        RealDesktopInput.SendKey(0x5B, true); // VK_LWIN

                    // Press main key
                    if (vk != 0)
                    {
                        RealDesktopInput.SendKey(vk, true);
                    }
                }
                else if (action == "UP")
                {
                    // Release main key
                    if (vk != 0)
                    {
                        RealDesktopInput.SendKey(vk, false);
                    }

                    // Release modifiers
                    if (modifiers.Contains("MODIFIER_LEFT_CTRL") || modifiers.Contains("MODIFIER_RIGHT_CTRL"))
                        RealDesktopInput.SendKey(0x11, false);
                    if (modifiers.Contains("MODIFIER_LEFT_ALT") || modifiers.Contains("MODIFIER_RIGHT_ALT"))
                        RealDesktopInput.SendKey(0x12, false);
                    if (modifiers.Contains("MODIFIER_LEFT_SHIFT") || modifiers.Contains("MODIFIER_RIGHT_SHIFT"))
                        RealDesktopInput.SendKey(0x10, false);
                    if (modifiers.Contains("MODIFIER_LEFT_GUI") || modifiers.Contains("MODIFIER_RIGHT_GUI"))
                        RealDesktopInput.SendKey(0x5B, false);
                }
            }
            catch (Exception ex)
            {
                Log("ProcessJsonEvent error: " + ex.Message);
            }
        }

        private void UpdateUiSafe(Action action)
        {
            try
            {
                if (uiContext != null)
                {
                    uiContext.Post(delegate(object state)
                    {
                        try { action(); } catch {}
                    }, null);
                }
                else
                {
                    action();
                }
            }
            catch (Exception)
            {
                // Context might be disposing
            }
        }

        private static void EnsureAutoStartup()
        {
            try
            {
                using (RegistryKey key = Registry.CurrentUser.OpenSubKey(RUN_REG_KEY, true))
                {
                    if (key != null && key.GetValue(APP_NAME) == null)
                    {
                        key.SetValue(APP_NAME, "\"" + Application.ExecutablePath + "\"");
                        Log("Auto-registered in HKCU Run registry: " + Application.ExecutablePath);
                    }
                }
            }
            catch (Exception ex)
            {
                Log("EnsureAutoStartup failed: " + ex.Message);
            }
        }

        private bool IsStartupEnabled()
        {
            try
            {
                using (RegistryKey key = Registry.CurrentUser.OpenSubKey(RUN_REG_KEY, false))
                {
                    if (key != null)
                    {
                        return key.GetValue(APP_NAME) != null;
                    }
                }
            }
            catch (Exception) {}
            return false;
        }

        private void OnStartupToggled(object sender, EventArgs e)
        {
            try
            {
                using (RegistryKey key = Registry.CurrentUser.OpenSubKey(RUN_REG_KEY, true))
                {
                    if (key != null)
                    {
                        if (startupMenuItem.Checked)
                        {
                            key.DeleteValue(APP_NAME, false);
                            startupMenuItem.Checked = false;
                        }
                        else
                        {
                            key.SetValue(APP_NAME, "\"" + Application.ExecutablePath + "\"");
                            startupMenuItem.Checked = true;
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Could not update startup setting: " + ex.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void OnReconnectClicked(object sender, EventArgs e)
        {
            try
            {
                if (currentClient != null)
                {
                    currentClient.Close();
                }
            }
            catch (Exception) {}
        }

        private void OnExitClicked(object sender, EventArgs e)
        {
            isRunning = false;
            try
            {
                if (currentClient != null)
                {
                    currentClient.Close();
                }
            }
            catch (Exception) {}

            RealDesktopInput.Stop();
            notifyIcon.Visible = false;
            Application.Exit();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                isRunning = false;
                if (notifyIcon != null)
                {
                    notifyIcon.Dispose();
                }
                if (iconConnected != null)
                {
                    iconConnected.Dispose();
                }
                if (iconDisconnected != null)
                {
                    iconDisconnected.Dispose();
                }
            }
            base.Dispose(disposing);
        }
    }

    public static class RealDesktopInput
    {
        [DllImport("user32.dll", SetLastError = true)]
        private static extern IntPtr OpenDesktop(string lpszDesktop, uint dwFlags, bool fInherit, uint dwDesiredAccess);

        [DllImport("user32.dll", SetLastError = true)]
        private static extern bool SetThreadDesktop(IntPtr hDesktop);

        [DllImport("user32.dll")]
        private static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, UIntPtr dwExtraInfo);

        [DllImport("user32.dll")]
        private static extern byte MapVirtualKey(uint uCode, uint uMapType);

        private const uint DESKTOP_ALL_ACCESS = 0x01FF;
        private const uint KEYEVENTF_KEYUP = 0x0002;
        private const uint KEYEVENTF_EXTENDEDKEY = 0x0001;

        private struct KeyEventItem
        {
            public byte vk;
            public bool isDown;
        }

        private static BlockingCollection<KeyEventItem> queue = new BlockingCollection<KeyEventItem>();
        private static Thread workerThread;
        private static volatile bool isRunning = false;

        public static void Start()
        {
            if (isRunning) return;
            isRunning = true;

            workerThread = new Thread(() =>
            {
                // Attach worker thread directly to the user's interactive desktop "Default"
                IntPtr hDesk = OpenDesktop("Default", 0, false, DESKTOP_ALL_ACCESS);
                if (hDesk != IntPtr.Zero)
                {
                    SetThreadDesktop(hDesk);
                }

                while (isRunning)
                {
                    try
                    {
                        KeyEventItem item;
                        if (queue.TryTake(out item, 500))
                        {
                            byte scan = MapVirtualKey(item.vk, 0);
                            uint flags = item.isDown ? 0 : KEYEVENTF_KEYUP;

                            // Extended keys (arrows, navigation, Win keys)
                            if ((item.vk >= 0x21 && item.vk <= 0x2E) || (item.vk >= 0x5B && item.vk <= 0x5D))
                            {
                                flags |= KEYEVENTF_EXTENDEDKEY;
                            }

                            keybd_event(item.vk, scan, flags, UIntPtr.Zero);
                        }
                    }
                    catch (Exception)
                    {
                        // Thread interruption or disposal
                    }
                }
            });
            workerThread.IsBackground = true;
            workerThread.Start();
        }

        public static void SendKey(ushort vkCode, bool isDown)
        {
            if (!isRunning) Start();
            queue.Add(new KeyEventItem { vk = (byte)vkCode, isDown = isDown });
        }

        public static void ReleaseAllModifiers()
        {
            SendKey(0x11, false); // Ctrl
            SendKey(0x12, false); // Alt
            SendKey(0x10, false); // Shift
            SendKey(0x5B, false); // Win
        }

        public static void Stop()
        {
            isRunning = false;
        }
    }

    public static class HidToVkMapper
    {
        private static readonly Dictionary<int, ushort> map = new Dictionary<int, ushort>()
        {
            // Letters A-Z (0x04 - 0x1D -> 0x41 - 0x5A)
            { 0x04, 0x41 }, { 0x05, 0x42 }, { 0x06, 0x43 }, { 0x07, 0x44 },
            { 0x08, 0x45 }, { 0x09, 0x46 }, { 0x0A, 0x47 }, { 0x0B, 0x48 },
            { 0x0C, 0x49 }, { 0x0D, 0x4A }, { 0x0E, 0x4B }, { 0x0F, 0x4C },
            { 0x10, 0x4D }, { 0x11, 0x4E }, { 0x12, 0x4F }, { 0x13, 0x50 },
            { 0x14, 0x51 }, { 0x15, 0x52 }, { 0x16, 0x53 }, { 0x17, 0x54 },
            { 0x18, 0x55 }, { 0x19, 0x56 }, { 0x1A, 0x57 }, { 0x1B, 0x58 },
            { 0x1C, 0x59 }, { 0x1D, 0x5A },

            // Numbers 1-9, 0 (0x1E - 0x27 -> 0x31 - 0x39, 0x30)
            { 0x1E, 0x31 }, { 0x1F, 0x32 }, { 0x20, 0x33 }, { 0x21, 0x34 },
            { 0x22, 0x35 }, { 0x23, 0x36 }, { 0x24, 0x37 }, { 0x25, 0x38 },
            { 0x26, 0x39 }, { 0x27, 0x30 },

            // Controls & Symbols
            { 0x28, 0x0D }, // Enter
            { 0x29, 0x1B }, // Escape
            { 0x2A, 0x08 }, // Backspace
            { 0x2B, 0x09 }, // Tab
            { 0x2C, 0x20 }, // Space
            { 0x2D, 0xBD }, // - (VK_OEM_MINUS)
            { 0x2E, 0xBB }, // = (VK_OEM_PLUS)
            { 0x2F, 0xDB }, // [ (VK_OEM_4)
            { 0x30, 0xDD }, // ] (VK_OEM_6)
            { 0x31, 0xDC }, // \ (VK_OEM_5)
            { 0x33, 0xBA }, // ; (VK_OEM_1)
            { 0x34, 0xDE }, // ' (VK_OEM_7)
            { 0x35, 0xC0 }, // ` (VK_OEM_3)
            { 0x36, 0xBC }, // , (VK_OEM_COMMA)
            { 0x37, 0xBE }, // . (VK_OEM_PERIOD)
            { 0x38, 0xBF }, // / (VK_OEM_2)
            { 0x39, 0x14 }, // Caps Lock
            { 0x46, 0x2C }, // Print Screen
            { 0x47, 0x91 }, // Scroll Lock
            { 0x48, 0x13 }, // Pause
            { 0x49, 0x2D }, // Insert
            { 0x4C, 0x2E }, // Delete

            // Navigation
            { 0x4F, 0x27 }, // Right
            { 0x50, 0x25 }, // Left
            { 0x51, 0x28 }, // Down
            { 0x52, 0x26 }, // Up
            { 0x4A, 0x24 }, // Home
            { 0x4D, 0x23 }, // End
            { 0x4B, 0x21 }, // Page Up
            { 0x4E, 0x22 }, // Page Down

            // Keypad (Numpad)
            { 0x53, 0x90 }, // Num Lock
            { 0x54, 0x6F }, // KP / (Divide)
            { 0x55, 0x6A }, // KP * (Multiply)
            { 0x56, 0x6D }, // KP - (Subtract)
            { 0x57, 0x6B }, // KP + (Add)
            { 0x58, 0x0D }, // KP Enter
            { 0x59, 0x61 }, // KP 1
            { 0x5A, 0x62 }, // KP 2
            { 0x5B, 0x63 }, // KP 3
            { 0x5C, 0x64 }, // KP 4
            { 0x5D, 0x65 }, // KP 5
            { 0x5E, 0x66 }, // KP 6
            { 0x5F, 0x67 }, // KP 7
            { 0x60, 0x68 }, // KP 8
            { 0x61, 0x69 }, // KP 9
            { 0x62, 0x60 }, // KP 0
            { 0x63, 0x6E }, // KP . (Decimal)

            // Function Keys F1-F12 (0x3A - 0x45 -> 0x70 - 0x7B)
            { 0x3A, 0x70 }, { 0x3B, 0x71 }, { 0x3C, 0x72 }, { 0x3D, 0x73 },
            { 0x3E, 0x74 }, { 0x3F, 0x75 }, { 0x40, 0x76 }, { 0x41, 0x77 },
            { 0x42, 0x78 }, { 0x43, 0x79 }, { 0x44, 0x7A }, { 0x45, 0x7B }
        };

        public static ushort GetVkCode(int hidCode)
        {
            ushort vk;
            if (map.TryGetValue(hidCode, out vk))
            {
                return vk;
            }
            return 0;
        }
    }
}
