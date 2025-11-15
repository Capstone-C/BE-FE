export default function Spinner({ size = 20 }: { size?: number }) {
  const border = Math.max(2, Math.round(size / 10));
  return (
    <div
      className="inline-block animate-spin rounded-full border-t-blue-600 border-gray-200"
      style={{ width: size, height: size, borderWidth: border }}
      role="status"
      aria-label="loading"
    />
  );
}
