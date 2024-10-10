// entities/card/cardBack.jsx
import { useNavigate } from "react-router-dom";
import { useNewsCardStore } from "./store/cardStore";
import { formatKoreanDateFull } from "shared/utils/dateUtils";

export const CardBack = ({ onClose }) => {
  const navigate = useNavigate();
  const { newsCard } = useNewsCardStore();
  const category = newsCard.category;
  const userDifficulty = newsCard.score;

  const handleCardNews = (id) => {
    onClose();
    navigate(`/news/detail/${id}`);
  };

  // 카테고리에 따른 배경색 설정
  const categoryBgColor =
    category === 0
      ? "bg-red-600"
      : category === 1
      ? "bg-green-600"
      : "bg-blue-600";

  // 카테고리에 따른 배경색 설정
  const categoryBtColor =
    category === 0
      ? "bg-red-700"
      : category === 1
      ? "bg-green-700"
      : "bg-blue-700";
  // 난이도 숫자를 한글로 변환
  const difficultyText =
    userDifficulty === 0
      ? "어려워요"
      : userDifficulty === 1
      ? "보통이예요"
      : "쉬워요";

  // console.log(category);

  return (
    <div
      className={`relative w-[360px] h-[480px] rounded-2xl flex flex-col items-center py-4 ${categoryBgColor}`}
    >
      <div className="flex flex-col items-center gap-y-[3px] px-5 w-full">
        <div className="flex flex-col items-center gap-y-[8px] w-full">
          <div className="text-white text-4xl font-card flex justify-center items-center h-16">
            Newzy
          </div>
          <div className="flex flex-col gap-y-2 w-full">
            <div className="w-full border border-gray-300"></div>
            <div className="w-full border border-gray-300"></div>
          </div>
        </div>

        <div className="px-2 pt-1 w-full">
          <div className="flex justify-end items-center gap-x-[6px] w-full pb-3 ">
            <div className="text-white text-[14px] font-[ChosunilboNM]">
              획득날짜 :
            </div>
            <div className="text-white text-[14px] font-[ChosunilboNM]">
              {formatKoreanDateFull(newsCard?.createdAt)}
            </div>
          </div>

          <div className="flex justify-end items-center gap-x-[6px] w-full">
            <div className="text-white text-[14px] font-[ChosunilboNM]">
              체감난이도 :
            </div>
            <div className="text-white text-[14px] font-[ChosunilboNM]">
              {difficultyText}
            </div>
          </div>
        </div>
      </div>

      <div className="px-9 pb-16 flex flex-grow justify-center items-center">
        <p className="text-white text-[16px] font-[ChosunilboNM] leading-[25px] tracking-wider">
          {newsCard?.summary}
        </p>
      </div>

      <div className="absolute px-8 w-full bottom-6 flex items-center">
        <button
          className={`w-full h-12 ${categoryBtColor} rounded-md  text-white text-lg font-semibold`}
          onClick={() => handleCardNews(newsCard.newsId)}
        >
          기사 다시 보기
        </button>
      </div>
    </div>
  );
};
