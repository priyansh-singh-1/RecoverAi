import { useEffect, useState, useRef } from 'react';
import { animate } from 'framer-motion';

export const useCountUp = (target, duration = 1.2) => {
  const [value, setValue] = useState(0);
  const prevTarget = useRef(0);

  useEffect(() => {
    if (target === 0) {
      setValue(0);
      prevTarget.current = 0;
      return;
    }

    const prefersReducedMotion =
      typeof window !== 'undefined' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReducedMotion) {
      setValue(target);
      prevTarget.current = target;
      return;
    }

    const controls = animate(prevTarget.current, target, {
      duration,
      onUpdate: (v) => setValue(v),
    });

    prevTarget.current = target;

    return () => controls.stop();
  }, [target, duration]);

  return value;
};
