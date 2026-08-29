'use client';

import React from 'react';
import katex from 'katex';

interface LatexRendererProps {
  content: string;
  className?: string;
}

export const LatexRenderer: React.FC<LatexRendererProps> = ({ content, className = '' }) => {
  if (!content) return null;

  // Split markdown text into block segments: block math ($$...$$), code blocks (```...```), and regular text blocks
  const parseBlocks = (raw: string) => {
    // Normalize newlines
    const normalized = raw.replace(/\r\n/g, '\n');

    // Split block math and code blocks
    const blockRegex = /(\$\$[\s\S]*?\$\$|```[\s\S]*?```)/g;
    const segments = normalized.split(blockRegex);

    return segments.map((segment, segIdx) => {
      if (!segment) return null;

      // 1. Block Math ($$...$$)
      if (segment.startsWith('$$') && segment.endsWith('$$')) {
        const math = segment.slice(2, -2).trim();
        try {
          const html = katex.renderToString(math, {
            displayMode: true,
            throwOnError: false,
          });
          return (
            <div
              key={`math-block-${segIdx}`}
              className="my-4 overflow-x-auto py-3 px-4 rounded-xl bg-surface-variant/70 border border-primary/20 shadow-inner font-serif text-text-primary text-base sm:text-lg flex justify-center items-center"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        } catch (_) {
          return (
            <pre key={`math-err-${segIdx}`} className="my-2 p-3 rounded-lg bg-surface-variant text-xs font-mono text-error overflow-x-auto">
              {segment}
            </pre>
          );
        }
      }

      // 2. Code Blocks (```...```)
      if (segment.startsWith('```') && segment.endsWith('```')) {
        const codeContent = segment.slice(3, -3);
        const firstLineEnd = codeContent.indexOf('\n');
        const lang = firstLineEnd !== -1 ? codeContent.slice(0, firstLineEnd).trim() : '';
        const code = firstLineEnd !== -1 ? codeContent.slice(firstLineEnd + 1) : codeContent;

        return (
          <div key={`code-block-${segIdx}`} className="my-3 rounded-xl overflow-hidden border border-border bg-surface-variant">
            {lang && (
              <div className="px-3 py-1 bg-surface border-b border-border text-[11px] font-mono text-text-secondary uppercase">
                {lang}
              </div>
            )}
            <pre className="p-3.5 text-xs sm:text-sm font-mono text-text-primary overflow-x-auto">
              <code>{code}</code>
            </pre>
          </div>
        );
      }

      // 3. Regular Markdown lines & inline elements
      const lines = segment.split('\n');
      return (
        <div key={`text-block-${segIdx}`} className="space-y-2">
          {renderLines(lines, segIdx)}
        </div>
      );
    });
  };

  // Helper to render lines (headings, lists, blockquotes, paragraphs)
  const renderLines = (lines: string[], blockIdx: number) => {
    const rendered: React.ReactNode[] = [];
    let listBuffer: { type: 'ul' | 'ol'; items: string[] } | null = null;

    const flushList = (keyPrefix: string) => {
      if (!listBuffer) return;
      const { type, items } = listBuffer;
      listBuffer = null;

      if (type === 'ol') {
        rendered.push(
          <ol key={`${keyPrefix}-ol`} className="my-2.5 pl-2 space-y-1.5 list-none">
            {items.map((it, idx) => (
              <li key={idx} className="flex items-start gap-2.5 text-sm sm:text-base leading-relaxed">
                <span className="w-5 h-5 rounded-full bg-primary/10 border border-primary/30 text-primary font-bold text-xs flex items-center justify-center shrink-0 mt-0.5 font-mono">
                  {idx + 1}
                </span>
                <div className="flex-1">{renderInline(it)}</div>
              </li>
            ))}
          </ol>
        );
      } else {
        rendered.push(
          <ul key={`${keyPrefix}-ul`} className="my-2.5 pl-2 space-y-1.5 list-none">
            {items.map((it, idx) => (
              <li key={idx} className="flex items-start gap-2.5 text-sm sm:text-base leading-relaxed">
                <span className="w-1.5 h-1.5 rounded-full bg-primary shrink-0 mt-2" />
                <div className="flex-1">{renderInline(it)}</div>
              </li>
            ))}
          </ul>
        );
      }
    };

    lines.forEach((line, lIdx) => {
      const trimmed = line.trim();

      if (!trimmed) {
        flushList(`flush-${lIdx}`);
        return;
      }

      // Headings
      if (trimmed.startsWith('### ')) {
        flushList(`h3-${lIdx}`);
        rendered.push(
          <h3
            key={`h3-${blockIdx}-${lIdx}`}
            className="text-base sm:text-lg font-bold text-text-primary mt-4 mb-2 flex items-center gap-2 border-b border-border/40 pb-1"
          >
            <span className="w-1.5 h-4 bg-primary rounded-full" />
            {renderInline(trimmed.slice(4))}
          </h3>
        );
        return;
      }

      if (trimmed.startsWith('## ')) {
        flushList(`h2-${lIdx}`);
        rendered.push(
          <h2
            key={`h2-${blockIdx}-${lIdx}`}
            className="text-lg sm:text-xl font-extrabold text-text-primary mt-5 mb-2.5 flex items-center gap-2 border-b border-border pb-1.5"
          >
            <span className="w-2 h-5 bg-primary rounded-full" />
            {renderInline(trimmed.slice(3))}
          </h2>
        );
        return;
      }

      if (trimmed.startsWith('# ')) {
        flushList(`h1-${lIdx}`);
        rendered.push(
          <h1
            key={`h1-${blockIdx}-${lIdx}`}
            className="text-xl sm:text-2xl font-black text-text-primary mt-6 mb-3 flex items-center gap-2.5 border-b border-border pb-2"
          >
            {renderInline(trimmed.slice(2))}
          </h1>
        );
        return;
      }

      // Callout alerts (e.g., ⚠️ Key Examination Trap:)
      if (trimmed.startsWith('⚠️') || trimmed.startsWith('💡') || trimmed.startsWith('📌') || trimmed.startsWith('> ')) {
        flushList(`callout-${lIdx}`);
        const isWarning = trimmed.startsWith('⚠️');
        const isLightbulb = trimmed.startsWith('💡');
        const contentStr = trimmed.startsWith('> ') ? trimmed.slice(2) : trimmed;

        rendered.push(
          <div
            key={`callout-${blockIdx}-${lIdx}`}
            className={`my-3 p-3.5 sm:p-4 rounded-xl border text-sm sm:text-base leading-relaxed ${
              isWarning
                ? 'bg-warning-container/20 border-warning/40 text-text-primary'
                : isLightbulb
                ? 'bg-primary-container/20 border-primary/40 text-text-primary'
                : 'bg-surface-variant border-border text-text-primary'
            }`}
          >
            {renderInline(contentStr)}
          </div>
        );
        return;
      }

      // Ordered list item (e.g., "1. ")
      const olMatch = trimmed.match(/^(\d+)\.\s+(.*)$/);
      if (olMatch) {
        if (!listBuffer || listBuffer.type !== 'ol') {
          flushList(`ol-switch-${lIdx}`);
          listBuffer = { type: 'ol', items: [] };
        }
        listBuffer.items.push(olMatch[2]);
        return;
      }

      // Unordered list item (e.g., "* ", "- ")
      const ulMatch = trimmed.match(/^[\*\-•]\s+(.*)$/);
      if (ulMatch) {
        if (!listBuffer || listBuffer.type !== 'ul') {
          flushList(`ul-switch-${lIdx}`);
          listBuffer = { type: 'ul', items: [] };
        }
        listBuffer.items.push(ulMatch[1]);
        return;
      }

      // Standard paragraph line
      flushList(`p-${lIdx}`);
      rendered.push(
        <p key={`p-${blockIdx}-${lIdx}`} className="text-sm sm:text-base leading-relaxed text-text-primary">
          {renderInline(trimmed)}
        </p>
      );
    });

    flushList(`final-${blockIdx}`);
    return rendered;
  };

  // Helper to render inline tokens: inline math ($...$), bold (**...**), italics (*...*), code (`...`)
  const renderInline = (text: string) => {
    // Tokenize by inline math $...$, code `...`, bold **...**, and italic *...*
    const tokens = text.split(/(\$[^\$\n]+?\$|`[^`\n]+?`|\*\*.*?\*\*|\*[^\*\n]+?\*)/g);

    return tokens.map((token, i) => {
      if (!token) return null;

      // Inline math
      if (token.startsWith('$') && token.endsWith('$') && token.length > 1) {
        const math = token.slice(1, -1).trim();
        try {
          const html = katex.renderToString(math, {
            displayMode: false,
            throwOnError: false,
          });
          return (
            <span
              key={i}
              className="mx-1 inline-block font-serif text-primary align-baseline"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        } catch (_) {
          return (
            <span key={i} className="font-mono text-xs text-error">
              {token}
            </span>
          );
        }
      }

      // Inline code
      if (token.startsWith('`') && token.endsWith('`') && token.length > 1) {
        return (
          <code
            key={i}
            className="px-1.5 py-0.5 mx-0.5 rounded-md bg-surface-variant border border-border text-primary font-mono text-xs sm:text-sm"
          >
            {token.slice(1, -1)}
          </code>
        );
      }

      // Bold
      if (token.startsWith('**') && token.endsWith('**') && token.length > 3) {
        return (
          <strong key={i} className="font-bold text-text-primary">
            {renderSubInline(token.slice(2, -2))}
          </strong>
        );
      }

      // Italic
      if (token.startsWith('*') && token.endsWith('*') && token.length > 2) {
        return (
          <em key={i} className="italic text-text-secondary">
            {renderSubInline(token.slice(1, -1))}
          </em>
        );
      }

      return token;
    });
  };

  // Sub-inline helper for handling inline math nested in bold/italic tags
  const renderSubInline = (text: string) => {
    const mathParts = text.split(/(\$[^\$\n]+?\$)/g);
    return mathParts.map((mp, mIdx) => {
      if (mp.startsWith('$') && mp.endsWith('$') && mp.length > 1) {
        const math = mp.slice(1, -1).trim();
        try {
          const html = katex.renderToString(math, {
            displayMode: false,
            throwOnError: false,
          });
          return (
            <span
              key={mIdx}
              className="mx-0.5 inline-block font-serif text-primary align-baseline"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        } catch (_) {
          return mp;
        }
      }
      return mp;
    });
  };

  return (
    <div className={`leading-relaxed break-words text-text-primary space-y-2 ${className}`}>
      {parseBlocks(content)}
    </div>
  );
};
