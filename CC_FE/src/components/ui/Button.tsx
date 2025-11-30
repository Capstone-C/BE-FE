import { ButtonHTMLAttributes, PropsWithChildren } from 'react';

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger' | 'outline';
type Size = 'sm' | 'md' | 'lg';

export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  className = '',
  ...rest
}: PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; size?: Size }>) {
  const base = 'inline-flex items-center justify-center font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed';
  
  const sizes: Record<Size, string> = {
    sm: 'rounded-lg px-3 py-1.5 text-sm',
    md: 'rounded-xl px-5 py-2.5 text-sm',
    lg: 'rounded-xl px-6 py-3 text-base',
  };
  
  const styles: Record<Variant, string> = {
    primary: 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-lg shadow-purple-500/30 hover:shadow-xl hover:shadow-purple-500/40 hover:scale-105 active:scale-95',
    secondary: 'bg-gradient-to-r from-gray-100 to-gray-200 text-gray-800 hover:from-gray-200 hover:to-gray-300 shadow-md hover:shadow-lg active:scale-95',
    ghost: 'bg-transparent text-gray-700 hover:bg-gray-100 hover:text-gray-900',
    danger: 'bg-gradient-to-r from-red-500 to-red-600 text-white shadow-lg shadow-red-500/30 hover:shadow-xl hover:shadow-red-500/40 hover:scale-105 active:scale-95',
    outline: 'border-2 border-purple-600 text-purple-600 hover:bg-purple-50 active:scale-95',
  };
  
  return (
    <button className={`${base} ${sizes[size]} ${styles[variant]} ${className}`} {...rest}>
      {children}
    </button>
  );
}
