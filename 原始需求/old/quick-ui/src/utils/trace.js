// trace.js
function randomHex(bytes) {
    const arr = new Uint8Array(bytes);
    crypto.getRandomValues(arr);
    return Array.from(arr, b => b.toString(16).padStart(2, '0')).join('');
}

export function createTraceContext() {
    const traceId = randomHex(16); // 16 bytes = 32 hex
    const spanId  = randomHex(8);  // 8 bytes  = 16 hex

    const traceparent = `00-${traceId}-${spanId}-01`;

    return {
        traceId,
        spanId,
        traceparent
    };
}
