// src/contexts/AutocompleteBlocker.tsx
import React, { useEffect } from 'react';

function isInsideAllowed(node: Element) {
  return !!node.closest('[data-allow-autocomplete="true"]');
}

function applyAllowAutocomplete(root: ParentNode) {
  const forms = root.querySelectorAll('form');
  forms.forEach((f) => {
    if (!(f instanceof HTMLFormElement)) return;
    f.setAttribute('autocomplete', 'on');
  });

  const inputs = root.querySelectorAll('input, textarea');
  inputs.forEach((el) => {
    if (!(el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement)) return;
    const isPassword = el instanceof HTMLInputElement && el.type === 'password';
    el.setAttribute('autocomplete', isPassword ? 'current-password' : 'on');
    el.removeAttribute('autocorrect');
    el.removeAttribute('autocapitalize');
    el.removeAttribute('spellcheck');
    // @ts-ignore
    if (el instanceof HTMLElement && el.style) el.style.webkitTextSecurity = '';
  });
}

function applyNoAutocomplete(root: ParentNode) {
  const forms = root.querySelectorAll('form');
  forms.forEach((f) => {
    if (!(f instanceof HTMLFormElement)) return;
    if (isInsideAllowed(f)) return; // whitelist
    if (f.autocomplete !== 'off') f.setAttribute('autocomplete', 'off');
  });

  const inputs = root.querySelectorAll('input, textarea');
  inputs.forEach((el) => {
    if (!(el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement)) return;
    if (isInsideAllowed(el)) return; // whitelist
    const isPassword = el instanceof HTMLInputElement && el.type === 'password';
    el.setAttribute('autocomplete', isPassword ? 'new-password' : 'off');
    el.setAttribute('autocorrect', 'off');
    el.setAttribute('autocapitalize', 'off');
    el.setAttribute('spellcheck', 'false');
    // @ts-ignore
    if (el instanceof HTMLElement && el.style) el.style.webkitTextSecurity = isPassword ? 'disc' : '';
  });
}

export const AutocompleteBlocker: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  useEffect(() => {
    // Initial pass: disable globally, but also re-enable inside allowed containers
    applyNoAutocomplete(document);
    document.querySelectorAll('[data-allow-autocomplete="true"]').forEach((container) => {
      if (container instanceof HTMLElement) {
        applyAllowAutocomplete(container);
      }
    });

    // Observe DOM changes to apply to future elements
    const observer = new MutationObserver((mutations) => {
      for (const m of mutations) {
        if (m.type === 'childList') {
          m.addedNodes.forEach((node) => {
            if (node instanceof HTMLElement || node instanceof DocumentFragment) {
              // Always apply disable first
              applyNoAutocomplete(node);
              // If this node or its subtree is marked allowed, re-enable within it
              const allowedContainers =
                node instanceof HTMLElement && node.matches('[data-allow-autocomplete="true"]')
                  ? [node]
                  : Array.from(node.querySelectorAll?.('[data-allow-autocomplete="true"]') ?? []);
              allowedContainers.forEach((c) => applyAllowAutocomplete(c));
            }
          });
        }
      }
    });

    observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
    });

    return () => observer.disconnect();
  }, []);

  return <>{children}</>;
};
