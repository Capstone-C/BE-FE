import { PropsWithChildren } from 'react';

export interface BadgeProps extends PropsWithChildren {
  variant?: 'default' | 'purple' | 'green' | 'blue' | 'gray';
  className?: string;
}

export default function Badge({ children, variant = 'default', className = '' }: BadgeProps) {
  const variantClasses = {
    default: 'bg-purple-50 text-purple-700 border-purple-200',
    purple: 'bg-purple-50 text-purple-700 border-purple-200',
    green: 'bg-green-50 text-green-700 border-green-200',
    blue: 'bg-blue-50 text-blue-700 border-blue-200',
    gray: 'bg-gray-50 text-gray-700 border-gray-200',
  };

  return (
    <span
      className={`
        inline-flex items-center gap-1
        px-2.5 py-1
        text-xs font-medium
        border rounded-full
        transition-colors
        ${variantClasses[variant]}
        ${className}
      `}
    >
      {children}
    </span>
  );
}
