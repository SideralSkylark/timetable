export enum Role {
  ADMIN = 'ADMIN',
  COORDINATOR = 'COORDINATOR',
  DIRECTOR = 'DIRECTOR',
  ASISTENT = 'ASISTENT', // Keeping the misspelling from the codebase for now
  TEACHER = 'TEACHER',
  STUDENT = 'STUDENT',
}

export type UserRole = keyof typeof Role
