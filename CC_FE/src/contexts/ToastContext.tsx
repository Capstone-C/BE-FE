// src/contexts/ToastContext.tsx
import React, { createContext, useContext, useState, useCallback } from 'react';

export interface Toast {
  id: number;
  message: string;
  type?: 'success' | 'error' | 'info';
  duration?: number; // ms
}

interface ToastContextValue {
  toasts: Toast[];
  show: (message: string, opts?: Omit<Toast, 'id' | 'message'> & { message?: string }) => void;
  remove: (id: number) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const remove = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const show = useCallback(
    (message: string, opts?: Omit<Toast, 'id' | 'message'> & { message?: string }) => {
      const id = Date.now() + Math.random();
      const toast: Toast = {
        id,
        message: opts?.message ?? message,
        type: opts?.type ?? 'info',
        duration: opts?.duration ?? 2500,
      };
      setToasts((prev) => [...prev, toast]);
      setTimeout(() => remove(id), toast.duration);
    },
    [remove],
  );

  return (
    <ToastContext.Provider value={{ toasts, show, remove }}>
      {children}
      <div className="fixed top-4 right-4 space-y-2 z-50">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`px-4 py-2 rounded shadow text-sm cursor-pointer transition-opacity bg-white border ${
              t.type === 'success'
                ? 'border-green-300 text-green-700'
                : t.type === 'error'
                  ? 'border-red-300 text-red-700'
                  : 'border-gray-300 text-gray-700'
            }`}
            onClick={() => remove(t.id)}
          >
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
