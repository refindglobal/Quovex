'use client';

import React from 'react';
import katex from 'katex';

interface LatexRendererProps {
  content: string;
  className?: string;
}

export const LatexRenderer: React.FC<LatexRendererProps> = ({ content, className = '' }) => {
  if (!content) return null;

  // Function to render math or text tokens
  const renderFormattedTokens = (text: string) => {
    // Split by block math $$...$$ first, then inline math $...$
    const parts = text.split(/(\$\$[\s\S]*?\$\$|\$[^\$\n]+?\$)/g);

    return parts.map((part, index) => {
      if (part.startsWith('$$') && part.endsWith('$$')) {
        const math = part.slice(2, -2).trim();
        try {
          const html = katex.renderToString(math, {
            displayMode: true,
            throwOnError: false,
          });
          return (
            <div
              key={index}
              className="my-3 overflow-x-auto py-1 text-center font-serif text-primary"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        } catch (_) {
          return <pre key={index} className="text-xs font-mono text-error">{part}</pre>;
        }
      } else if (part.startsWith('$') && part.endsWith('$')) {
        const math = part.slice(1, -1).trim();
        try {
          const html = katex.renderToString(math, {
            displayMode: false,
            throwOnError: false,
          });
          return (
            <span
              key={index}
              className="mx-0.5 inline-block font-serif text-primary"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        } catch (_) {
          return <span key={index} className="font-mono text-xs text-error">{part}</span>;
        }
      }

      // Format markdown bold **...** and linebreaks
      const lines = part.split('\n');
      return (
        <span key={index}>
          {lines.map((line, lIdx) => {
            const boldParts = line.split(/(\*\*.*?\*\*)/g);
            return (
              <React.Fragment key={lIdx}>
                {lIdx > 0 && <br />}
                {boldParts.map((bp, bIdx) => {
                  if (bp.startsWith('**') && bp.endsWith('**')) {
                    return (
                      <strong key={bIdx} className="font-extrabold text-text-primary">
                        {bp.slice(2, -2)}
                      </strong>
                    );
                  }
                  return bp;
                })}
              </React.Fragment>
            );
          })}
        </span>
      );
    });
  };

  return (
    <div className={`leading-relaxed break-words text-text-primary ${className}`}>
      {renderFormattedTokens(content)}
    </div>
  );
};
