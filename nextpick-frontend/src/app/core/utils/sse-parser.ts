export interface SseEvent {
  event: string;
  data: string;
}

/** Incremental SSE parser that tolerates arbitrary network chunk boundaries. */
export class SseParser {
  private buffer = '';

  feed(chunk: string, flush = false): SseEvent[] {
    this.buffer += chunk.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
    const blocks = this.buffer.split('\n\n');
    const remainder = blocks.pop() ?? '';
    this.buffer = flush ? '' : remainder;
    if (flush && remainder) blocks.push(remainder);
    return blocks.filter(Boolean).map((block) => this.parseBlock(block)).filter((event): event is SseEvent => !!event);
  }

  private parseBlock(block: string): SseEvent | null {
    let event = 'message';
    const data: string[] = [];
    for (const line of block.split('\n')) {
      if (!line || line.startsWith(':')) continue;
      if (line.startsWith('event:')) event = line.slice(6).trimStart();
      if (line.startsWith('data:')) {
        const raw = line.slice(5);
        data.push(raw.startsWith(' ') ? raw.slice(1) : raw);
      }
    }
    return data.length ? { event, data: data.join('\n') } : null;
  }
}
