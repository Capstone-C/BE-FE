import { PropsWithChildren } from 'react';

export function Card({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`rounded-lg border bg-white shadow-sm ${className}`}>{children}</div>;
}

export function CardHeader({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`px-4 py-3 border-b ${className}`}>{children}</div>;
}

export function CardContent({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`px-4 py-3 ${className}`}>{children}</div>;
}

export function CardFooter({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`px-4 py-3 border-t ${className}`}>{children}</div>;
}
