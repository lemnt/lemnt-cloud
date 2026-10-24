import type { ReactNode } from 'react';
import styles from './formSection.module.css';

interface FormSectionProps {
  number: number;
  title: string;
  description?: string;
  children: ReactNode;
}

export function FormSection({ number, title, description, children }: FormSectionProps) {
  return (
    <section className={styles.section} aria-label={title}>
      <header className={styles.heading}>
        <span className={styles.number} aria-hidden="true">
          {number}
        </span>
        <div>
          <h2>{title}</h2>
          {description && <p>{description}</p>}
        </div>
      </header>
      {children}
    </section>
  );
}
