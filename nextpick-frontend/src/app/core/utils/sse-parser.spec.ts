import { SseParser } from './sse-parser';

describe('SseParser', () => {
  it('reassembles events split across arbitrary chunks', () => {
    const parser = new SseParser();

    expect(parser.feed('event: del')).toEqual([]);
    expect(parser.feed('ta\ndata: {"text":"hola')).toEqual([]);
    expect(parser.feed(' mundo"}\n\n')).toEqual([
      { event: 'delta', data: '{"text":"hola mundo"}' },
    ]);
  });

  it('preserves meaningful spaces and joins multiline data', () => {
    const parser = new SseParser();
    expect(parser.feed('data:  empieza\ndata: termina \n\n')).toEqual([
      { event: 'message', data: ' empieza\ntermina ' },
    ]);
  });

  it('flushes a final event without a trailing blank line', () => {
    const parser = new SseParser();
    expect(parser.feed('event: done\ndata: {}', true)).toEqual([
      { event: 'done', data: '{}' },
    ]);
  });
});
