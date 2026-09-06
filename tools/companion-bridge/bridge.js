/**
 * FlexKeypad Desktop Companion Bridge (Node.js)
 * ----------------------------------------------
 * Connects to the FlexKeypad Android app via USB port forwarding
 * and receives live keystroke events.
 */

const net = require('net');
const { execSync } = require('child_process');

const PORT = 8899;
const HOST = '127.0.0.1';

console.log('==================================================');
console.log('   FlexKeypad Desktop Companion Bridge (Node.js)  ');
console.log('==================================================');
console.log(`Connecting to FlexKeypad at ${HOST}:${PORT}...`);

// Map HID keycodes to characters/names
const KEY_MAP = {
    0x04: 'A', 0x05: 'B', 0x06: 'C', 0x07: 'D', 0x08: 'E', 0x09: 'F',
    0x0A: 'G', 0x0B: 'H', 0x0C: 'I', 0x0D: 'J', 0x0E: 'K', 0x0F: 'L',
    0x10: 'M', 0x11: 'N', 0x12: 'O', 0x13: 'P', 0x14: 'Q', 0x15: 'R',
    0x16: 'S', 0x17: 'T', 0x18: 'U', 0x19: 'V', 0x1A: 'W', 0x1B: 'X',
    0x1C: 'Y', 0x1D: 'Z',
    0x1E: '1', 0x1F: '2', 0x20: '3', 0x21: '4', 0x22: '5',
    0x23: '6', 0x24: '7', 0x25: '8', 0x26: '9', 0x27: '0',
    0x28: '{ENTER}', 0x29: '{ESC}', 0x2A: '{BACKSPACE}', 0x2B: '{TAB}', 0x2C: ' ',
    0x4C: '{DELETE}', 0x4F: '{RIGHT}', 0x50: '{LEFT}', 0x51: '{DOWN}', 0x52: '{UP}',
    0x3A: '{F1}', 0x3B: '{F2}', 0x3C: '{F3}', 0x3D: '{F4}', 0x3E: '{F5}', 0x3F: '{F6}',
    0x40: '{F7}', 0x41: '{F8}', 0x42: '{F9}', 0x43: '{F10}', 0x44: '{F11}', 0x45: '{F12}'
};

function connect() {
    const client = new net.Socket();

    client.connect(PORT, HOST, () => {
        console.log(`[CONNECTED] Successfully connected to FlexKeypad on port ${PORT}!`);
        console.log('Listening for button presses...\n');
    });

    let buffer = '';

    client.on('data', (data) => {
        buffer += data.toString('utf8');
        while (buffer.includes('\n')) {
            const newlineIndex = buffer.indexOf('\n');
            const line = buffer.slice(0, newlineIndex).trim();
            buffer = buffer.slice(newlineIndex + 1);

            if (!line) continue;

            try {
                const event = JSON.parse(line);
                const keyName = KEY_MAP[event.keyCode] || `Key 0x${event.keyCode.toString(16)}`;
                const mods = (event.modifiers && event.modifiers.length > 0) ? ` [${event.modifiers.join(', ')}]` : '';
                
                console.log(`[${event.action}] ${keyName}${mods}`);
            } catch (e) {
                // Ignore parse errors
            }
        }
    });

    client.on('close', () => {
        console.log('[DISCONNECTED] Connection closed. Reconnecting in 2s...');
        setTimeout(connect, 2000);
    });

    client.on('error', (err) => {
        if (err.code === 'ECONNREFUSED') {
            process.stdout.write('[RETRY] Waiting for FlexKeypad connection...\r');
        } else {
            console.log(`[ERROR] ${err.message}`);
        }
        setTimeout(connect, 2000);
    });
}

connect();
