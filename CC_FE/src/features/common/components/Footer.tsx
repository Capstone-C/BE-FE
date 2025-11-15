import Container from '@/components/ui/Container';

export function Footer() {
  return (
    <footer className="border-t bg-white">
      <Container>
        <div className="h-14 flex items-center justify-between text-sm text-gray-600">
          <span>© {new Date().getFullYear()} Capstone</span>
          <span className="text-gray-400">All rights reserved.</span>
        </div>
      </Container>
    </footer>
  );
}
