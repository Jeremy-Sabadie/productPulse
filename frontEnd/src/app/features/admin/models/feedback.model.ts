/**
 * Statuts possibles d'un feedback ProductPulse.
 */
export type FeedbackStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type FeedbackCategory = 'BUG' | 'IMPROVEMENT' | 'FEATURE';

/**
 * Représente un retour utilisateur renvoyé par l'API.
 */
export interface Feedback {
  id: number;
  title: string;
  description: string;
  applicationCode: string;
  authorId: number;
  createdAt: string;
  status: FeedbackStatus;
  category: FeedbackCategory;
  githubIssueNumber: number | null;
  githubIssueUrl: string | null;
}

/**
 * Réponse de création d'une issue GitHub.
 */
export interface GitHubIssueResult {
  number: number;
  url: string;
}

/**
 * Réponse paginée de Spring Data.
 */
export interface FeedbackPage {
  content: Feedback[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
