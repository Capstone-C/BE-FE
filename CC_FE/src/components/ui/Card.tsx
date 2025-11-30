import { PropsWithChildren } from 'react';

export function Card({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`rounded-2xl border border-gray-200 bg-white shadow-lg hover:shadow-xl transition-shadow duration-300 ${className}`}>{children}</div>;
}

export function CardHeader({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`px-6 py-4 border-b border-gray-100 ${className}`}>{children}</div>;
}

export function CardContent({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`px-6 py-4 ${className}`}>{children}</div>;
}

export function CardFooter({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return <div className={`px-6 py-4 border-t border-gray-100 ${className}`}>{children}</div>;
}
