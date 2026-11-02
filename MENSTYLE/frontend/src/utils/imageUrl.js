const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const configuredMediaBaseUrl = import.meta.env.VITE_MEDIA_BASE_URL;

function getOrigin(value) {
  try {
    return new URL(value, window.location.origin).origin;
  } catch {
    return window.location.origin;
  }
}

const mediaOrigin = getOrigin(configuredMediaBaseUrl || apiBaseUrl);

export function resolveImageUrl(value) {
  if (!value || typeof value !== 'string') return '';

  const imageUrl = value.trim();
  if (!imageUrl) return '';
  if (/^https?:\/\//i.test(imageUrl)) return imageUrl;
  if (imageUrl.startsWith('/images/products/')) return imageUrl;
  if (imageUrl === '/media' || imageUrl.startsWith('/media/')) {
    return `${mediaOrigin}${imageUrl}`;
  }

  return imageUrl;
}
