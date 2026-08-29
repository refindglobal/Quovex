/**
 * Quovex Admin — PII Masking & Privacy Shield Engine
 * Redacts student personal data (emails, phone numbers, IP addresses)
 * based on administrator role privilege.
 */

export type UserRole = 'superadmin' | 'ops' | 'support' | 'moderator';

export interface MaskingOptions {
  role?: UserRole;
  showFullForSuperAdmin?: boolean;
}

/**
 * Masks an email address: student.name@gmail.com -> s***e@gmail.com
 */
export function maskEmail(email: string | null | undefined, options: MaskingOptions = {}): string {
  if (!email) return 'N/A';
  if (options.role === 'superadmin' && options.showFullForSuperAdmin !== false) {
    return email;
  }

  const [username, domain] = email.split('@');
  if (!domain) return '***';

  if (username.length <= 2) {
    return `${username[0]}***@${domain}`;
  }

  const firstChar = username[0];
  const lastChar = username[username.length - 1];
  return `${firstChar}***${lastChar}@${domain}`;
}

/**
 * Masks a phone number: +91 9876543210 -> +91 ******3210
 */
export function maskPhone(phone: string | null | undefined, options: MaskingOptions = {}): string {
  if (!phone) return 'N/A';
  if (options.role === 'superadmin' && options.showFullForSuperAdmin !== false) {
    return phone;
  }

  const clean = phone.trim();
  if (clean.length < 6) return '******';

  const visibleDigits = 4;
  const maskedLength = clean.length - visibleDigits;
  return '*'.repeat(maskedLength) + clean.slice(-visibleDigits);
}

/**
 * Masks an IP address: 192.168.1.100 -> 192.168.***.***
 */
export function maskIp(ip: string | null | undefined, options: MaskingOptions = {}): string {
  if (!ip) return '0.0.0.0';
  if (options.role === 'superadmin' && options.showFullForSuperAdmin !== false) {
    return ip;
  }

  const parts = ip.split('.');
  if (parts.length === 4) {
    return `${parts[0]}.${parts[1]}.***.***`;
  }
  return '***.***.***.***';
}

/**
 * Masks user record object for API outputs
 */
export function maskUserRecord<T extends Record<string, any>>(user: T, options: MaskingOptions = {}): T {
  const result: Record<string, any> = { ...user };
  if ('email' in result && typeof result.email === 'string') {
    result.email = maskEmail(result.email, options);
  }
  if ('phoneNumber' in result && typeof result.phoneNumber === 'string') {
    result.phoneNumber = maskPhone(result.phoneNumber, options);
  }
  if ('ipAddress' in result && typeof result.ipAddress === 'string') {
    result.ipAddress = maskIp(result.ipAddress, options);
  }
  return result as T;
}
