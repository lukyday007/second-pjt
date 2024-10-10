import { useNavigate } from "react-router-dom";
import { TodayNews } from "./todayNews";

export const Header = () => {
  const navigate = useNavigate();
  const handleNewsCategory = (category) => navigate(`/news/${category}`);

  const now = new Date();
  const days = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];
  const today = `${now.getFullYear()}. ${
    now.getMonth() + 1
  }. ${now.getDate()}. ${days[now.getDay()]}`;

  const CategoryButton = ({ color, label }) => (
    <button
      className={`w-full h-[64px] flex justify-center items-center rounded-l-full`}
      style={{ backgroundColor: color }}
      onClick={() => handleNewsCategory(label)}
    >
      <svg className="w-[94px] h-[94px]"></svg>
      <div className="text-center text-[32px] font-semibold leading-[20px]">
        {label}
      </div>
    </button>
  );

  return (
    <div className="relative h-[440px] bg-gray-800">
      <div className="absolute top-0 left-0 w-full h-[200px]">
        <div className="relative flex items-center justify-center text-white text-center w-full h-full text-[140px] font-semibold font-header tracking-[-0.05em]">
          Newzy
        </div>
        <div className="absolute bottom-[7px] left-1/3 flex items-center text-white text-2xl font-semibold font-header">
          {today}
        </div>
      </div>

      <TodayNews />

      <div className="absolute bottom-0 right-0 flex flex-col justify-end items-center gap-3 pb-4">
        <div className="w-[256px] h-[228px] flex flex-col items-center gap-[18px]">
          <CategoryButton color="#ef8885" label="경제" />
          <CategoryButton color="#93cf96" label="사회" />
          <CategoryButton color="#78b7ef" label="세계" />
        </div>
      </div>
    </div>
  );
};
