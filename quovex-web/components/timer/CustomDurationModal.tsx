'use client';

import React, { useState } from 'react';
import { QuovexButton } from '../ui/QuovexButton';

interface CustomDurationModalProps {
  initialFocusMinutes: number;
  initialBreakMinutes: number;
  onConfirm: (focusMinutes: number, breakMinutes: number) => void;
  onClose: () => void;
}

export const CustomDurationModal: React.FC<CustomDurationModalProps> = ({
  initialFocusMinutes,
  initialBreakMinutes,
  onConfirm,
  onClose,
}) => {
  const [focusMins, setFocusMins] = useState(initialFocusMinutes || 30);
  const [breakMins, setBreakMins] = useState(initialBreakMinutes || 5);

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-surface border border-border rounded-3xl max-w-sm w-full p-6 space-y-6 shadow-2xl">
        <div>
          <h3 className="text-lg font-black text-text-primary">Custom Focus Duration</h3>
          <p className="text-xs text-text-secondary mt-0.5">Customize your study and break intervals</p>
        </div>

        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-xs font-bold text-text-secondary mb-1">
              <span>Focus Time</span>
              <span className="text-primary">{focusMins} minutes</span>
            </div>
            <input
              type="range"
              min={5}
              max={240}
              step={5}
              value={focusMins}
              onChange={(e) => setFocusMins(Number(e.target.value))}
              className="w-full accent-[#00C896] bg-surface-variant cursor-pointer"
            />
          </div>

          <div>
            <div className="flex justify-between text-xs font-bold text-text-secondary mb-1">
              <span>Break Time</span>
              <span className="text-secondary">{breakMins} minutes</span>
            </div>
            <input
              type="range"
              min={1}
              max={60}
              step={1}
              value={breakMins}
              onChange={(e) => setBreakMins(Number(e.target.value))}
              className="w-full accent-[#00C896] bg-surface-variant cursor-pointer"
            />
          </div>
        </div>

        <div className="flex gap-2">
          <QuovexButton variant="secondary" className="flex-1 text-xs" onClick={onClose}>
            Cancel
          </QuovexButton>
          <QuovexButton
            variant="primary"
            className="flex-1 text-xs"
            onClick={() => onConfirm(focusMins, breakMins)}
          >
            Apply Preset
          </QuovexButton>
        </div>
      </div>
    </div>
  );
};
