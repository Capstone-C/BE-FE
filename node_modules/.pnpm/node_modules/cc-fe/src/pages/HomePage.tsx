import { Link } from 'react-router-dom';

const HomePage = () => {
  return (
    <div>
      <h1>홈 페이지</h1>
      <Link to="/login">로그인 페이지로 이동</Link>
    </div>
  );
};

export default HomePage;
