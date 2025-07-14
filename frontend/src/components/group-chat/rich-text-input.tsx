'use client';

import { useRef, useEffect, forwardRef } from 'react';
import { cn } from '@/lib/utils';

interface RichTextInputProps {
  value: string;
  onChange: (value: string) => void;
  onKeyDown?: (event: React.KeyboardEvent) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

const RichTextInput = forwardRef<HTMLTextAreaElement, RichTextInputProps>((
  {
    value,
    onChange,
    onKeyDown,
    placeholder,
    disabled,
    className,
    style
  },
  ref
) => {
  const internalRef = useRef<HTMLTextAreaElement>(null);
  const textareaRef = (ref as React.RefObject<HTMLTextAreaElement>) || internalRef;

  // 自动调整高度，最大3行
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      const scrollHeight = textareaRef.current.scrollHeight;
      // 计算3行的高度：行高约24px，3行约72px
      const maxHeight = 72;
      textareaRef.current.style.height = `${Math.min(scrollHeight, maxHeight)}px`;
    }
  }, [value]);

  return (
    <textarea
      ref={textareaRef}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onKeyDown={onKeyDown}
      placeholder={placeholder}
      disabled={disabled}
      className={cn(
        'w-full resize-none border-0 bg-transparent outline-none focus:ring-0',
        '[&::-webkit-scrollbar]:hidden',
        className
      )}
      style={{
        ...style,
        display: 'flex',
        alignItems: 'center',
        overflow: 'hidden',
        scrollbarWidth: 'none',
        msOverflowStyle: 'none'
      }}
      rows={1}
    />
  );
});

RichTextInput.displayName = 'RichTextInput';

export default RichTextInput;