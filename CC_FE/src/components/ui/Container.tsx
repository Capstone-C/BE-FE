import { PropsWithChildren } from 'react';

export default function Container({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`mx-auto w-full max-w-6xl px-4 ${className}`}>{children}</div>;
}
