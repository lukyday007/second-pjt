import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { getGrade } from "shared/getGrade";
import { MdAdd, MdDelete } from "react-icons/md";
import MenuBar from "pages/profile/ui/menuBar";
import MyNewzy from "pages/profile/ui/myNewzy";
import BookMark from "pages/profile/ui/bookMark";
import Words from "pages/profile/ui/words";
import FollowIndexModal from "pages/profile/ui/followIndexModal";
import CardListModal from "pages/profile/ui/cardListModal";

import userProfile from "shared/images/user.png";
import cards from "shared/images/cards.svg";

import "./profile.css"

const getZoomLevel = () => {
  return window.devicePixelRatio * 100;
};

// 임시 유저 더미데이터
const user = {
  name: "정지훈",
  exp: 7023,
  introduce: `안녕하세요\n잘 부탁드립니다!!`,
  img: null,
  followers: 42,
  newzy: 7,
  followings: 25,
  birth: "2001-03-05",
};

const gradeDescriptions = {
  1: (
    <>
      <p className="font-semibold">Level 1 초보 뉴포터</p>
      시작하는 단계입니다!
    </>
  ),
  2: (
    <>
      <p className="font-semibold">Level 2 중급 뉴포터</p>
      꽤 실력이 있어요!
    </>
  ),
  3: (
    <>
      <p className="font-semibold">Level 3 고급 뉴포터</p>
      실력이 뛰어나요!
    </>
  ),
  4: (
    <>
      <p className="font-semibold">Level 4 마스터 뉴포터</p>
      당신은 프로입니다!
    </>
  ),
};

// exp 값을 기준으로 grade를 구하는 함수
const getGradeByExp = (exp) => {
  if (exp >= 10000) return 4;
  if (exp >= 500) return 3;
  if (exp >= 10) return 2;
  return 1;
};

// 각 grade의 최대 경험치값
const maxExpByGrade = {
  1: 10,    // 0~9: Level 1
  2: 500,   // 10~499: Level 2
  3: 10000, // 500~9999: Level 3
  4: user.exp, // 10000 이상: Level 4
};

export const Profile = () => {
  const navigate = useNavigate(); // useNavigate 훅 사용
  const location = useLocation(); // 현재 경로를 가져오기 위해 useLocation 훅 사용

  const [selectedMenu, setSelectedMenu] = useState(0);
  const [isModalOpen, setModalOpen] = useState(false); // 모달 상태 관리
  const [isCardListModalOpen, setIsCardListModalOpen] = useState(false); // CardListModa
  const [isTooltipVisible, setIsTooltipVisible] = useState(false); // 말풍선 상태 관리

  // 현재 경로에 따라 메뉴를 선택 상태로 설정
  useEffect(() => {
    switch (location.pathname) {
      case "/profile/myNewzy":
        setSelectedMenu(0);
        break;
      case "/profile/bookMark":
        setSelectedMenu(1);
        break;
      case "/profile/words":
        setSelectedMenu(2);
        break;
      default:
        setSelectedMenu(0);
        break;
    }
  }, [location]);

  // 유저의 grade 및 비중 계산
  const userGrade = getGradeByExp(user.exp);
  const maxExp = maxExpByGrade[userGrade];
  const expRatio = (user.exp / maxExp) * 100; // 현재 경험치 비중 계산 (퍼센트 값)

  // 편집 모드 관리
  const [isEditing, setIsEditing] = useState(false);
  const [profileData, setProfileData] = useState({
    name: user.name,
    introduce: user.introduce,
    img: user.img,
    birth: user.birth,
  });

  // SVG에서 사용할 경험치바 계산
  const radius = 135;
  const circumference = 2 * Math.PI * radius;
  const progress = (expRatio / 100) * circumference; // 현재 경험치에 해당하는 원형 길이

  // 글자수 제한
  const maxIntroduceLength = 30;

  // 모달 열기 및 닫기 함수
  const openModal = () => setModalOpen(true);
  const closeModal = () => setModalOpen(false);

  // CardListModal 열기 및 닫기 함수
  const openCardListModal = () => setIsCardListModalOpen(true);
  const closeCardListModal = () => setIsCardListModalOpen(false);

  // 저장 버튼 클릭 시 변경 사항 저장
  const handleSave = () => {
    console.log("저장된 데이터:", profileData);
    setIsEditing(false);
  };

  // 이미지 변경 핸들러
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setProfileData({ ...profileData, img: URL.createObjectURL(file) });
    }
  };

  // 이미지 제거 핸들러
  const handleImageRemove = () => {
    setProfileData({ ...profileData, img: null });
  };

  // 메뉴 클릭 시 경로를 변경
  const handleMenuChange = (menuIndex) => {
    setSelectedMenu(menuIndex);
    switch (menuIndex) {
      case 0:
        navigate("/profile/myNewzy"); // 프로필 페이지의 기본 경로로 이동
        break;
      case 1:
        navigate("/profile/bookMark"); // 북마크 경로로 이동
        break;
      case 2:
        navigate("/profile/words"); // 단어 페이지로 이동
        break;
      default:
        break;
    }
  };

  // 선택된 메뉴에 따라 다른 컴포넌트 렌더링
  const renderContent = () => {
    switch (selectedMenu) {
      case 0:
        return <MyNewzy />;
      case 1:
        return <BookMark />;
      case 2:
        return <Words />;
      default:
        return null;
    }
  };

  const [paddingX, setPaddingX] = useState(32); // 기본 패딩

  // 패딩 업데이트 로직을 추가합니다.
  useEffect(() => {
    const updatePaddingBasedOnZoom = () => {
      const zoomLevel = getZoomLevel();
  
      if (zoomLevel <= 90) {
        setPaddingX(300); // 줌 레벨이 80% 이하일 때 80px
      } else if (zoomLevel <= 100) {
        setPaddingX(150); // 줌 레벨이 80% ~ 90%일 때 56px
      } else if (zoomLevel <= 120) {
        setPaddingX(100); // 줌 레벨이 90% ~ 100%일 때 32px
      } else {
        setPaddingX(32); // 줌 레벨이 100% 이상일 때 16px
      }
    };

    // 초기 실행
    updatePaddingBasedOnZoom();

    // 매 500ms마다 줌 레벨을 확인하여 패딩을 업데이트
    const intervalId = setInterval(updatePaddingBasedOnZoom, 500);

    // cleanup 함수로 interval 제거
    return () => {
      clearInterval(intervalId);
    };
  }, []);

  return (
    <div className="overflow-x-auto bg-[#FFFFFF]">
      <div id="target" className="h-[409px] bg-[#132956] relative flex mb-12" style={{ paddingLeft: `${paddingX}px`, paddingRight: `${paddingX}px` }}>
        <div className="relative">
          {/* 경험치량 표시 */}
          <div
            className="absolute top-[10px] left-[150px] w-full flex justify-center items-center text-yellow-500 text-base font-extrabold tracking-wide whitespace-nowrap"
            style={{ textShadow: "2px 2px 4px rgba(0, 0, 0, 0.6)" }}
            >
            {user.exp} / {maxExp}
          </div>
          {/* SVG로 경험치 바 추가 */}
          <svg width="310" height="310" className="absolute top-[35px] left-[0px]" style={{ transform: "rotate(-90deg)" }}>
            <circle
              cx="150"
              cy="150"
              r={radius}
              stroke="gray"
              strokeWidth="24"
              fill="none"
              className="opacity-20"
            />
            <circle
              cx="150"
              cy="150"
              r={radius}
              stroke="gold"
              strokeWidth="24"
              fill="none"
              strokeDasharray={circumference}
              strokeDashoffset={circumference - progress}
              strokeLinecap="round"
              className="transition-all duration-500"
            />
          </svg>

          {/* 프로필 이미지 */}
          <div className={"absolute top-[70px] left-[25px] w-[250px] h-[250px] rounded-full flex items-center justify-center"}>
            <img
              src={profileData.img || userProfile}
              className={`w-full h-full object-cover rounded-full ${isEditing ? 'opacity-60' : ''}`}
              alt="프로필 이미지"
            />
            {isEditing && (
              <div className="absolute top-[105px] right-111 flex gap-12">
                <label htmlFor="profile-upload" className="cursor-pointer">
                  <MdAdd className="text-white bg-blue-500 rounded-full w-7 h-7" />
                  <input
                    id="profile-upload"
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleImageChange}
                  />
                </label>
                <MdDelete
                  className="text-white bg-red-500 rounded-full w-7 h-7 cursor-pointer"
                  onClick={handleImageRemove}
                />
              </div>
            )}
          </div>

          <div className="absolute top-[285px] left-[218px] w-[100px] h-[100px] bg-white bg-opacity-20 rounded-full flex items-center justify-center">
          <img
              src={getGrade(userGrade)}
              className="w-[60px] h-[60px] object-cover cursor-pointer"
              onMouseEnter={() => setIsTooltipVisible(true)}
              onMouseLeave={() => setIsTooltipVisible(false)}
            />
            {isTooltipVisible && (
              <div
                className="absolute top-[110%] transform -translate-x-1/2 bg-gray-700 text-white text-base rounded-lg p-2 opacity-0 animate-tooltip shadow-md"
                style={{ whiteSpace: "nowrap" }}
              >
                {gradeDescriptions[userGrade]}
                <span className="absolute top-[-5px] left-1/2 transform -translate-x-1/2 w-0 h-0 border-l-8 border-r-8 border-b-8 border-transparent border-b-gray-700"></span>
              </div>
            )}
          </div>

          {isEditing && (
            <input
              type="date"
              className="absolute top-[360px] left-[10px] w-[140px] px-2 py-1 rounded-md bg-gray-800 text-white"
              value={profileData.birth}
              onChange={(e) => setProfileData({ ...profileData, birth: e.target.value })}
            />
          )}
        </div>

        <div className="ml-[380px]">
          <div className="h-[100px] mt-[80px] text-white font-[Open Sans] text-[46px] leading-[24px] font-semibold flex items-center">
            {isEditing ? (
              <input
                type="text"
                value={profileData.name}
                onChange={(e) => setProfileData({ ...profileData, name: e.target.value })}
                className="outline-none w-full p-2 mr-5 text-white bg-gray-800 opacity-100"
              />
            ) : (
              <p className="p-2">{profileData.name}</p>
            )}
          </div>

          <div className="w-[250px] h-[200px] text-white font-[Open Sans] text-[24px] leading-[36px] font-semibold flex items-center text-left break-words whitespace-pre-wrap">
            {isEditing ? (
              <textarea
                value={profileData.introduce}
                onChange={(e) => {
                  const lines = e.target.value.split('\n');
                  // 최대 5줄까지만 입력 가능하도록 제한
                  if (lines.length <= 5) {
                    setProfileData({ ...profileData, introduce: e.target.value });
                  }
                }}
                rows={5} // 기본 5줄
                maxLength={maxIntroduceLength}
                className="outline-none w-full h-[200px] p-2 text-white bg-gray-800 opacity-100"
                placeholder={`자기소개 문구를\n작성해주세요!`}
              />
            ) : (
              <p className="p-2">{profileData.introduce}</p>
            )}
          </div>
          {isEditing && (
            <div className="font-semibold text-gray-200 text-left">
              {profileData.introduce.length}/{maxIntroduceLength}
            </div>
          )}
        </div>

        {/* Followers, Followings, Newzy 요소 */}
        <div className="flex flex-col items-center ml-auto mt-auto">
          <div className="flex gap-9">
            <div className="flex flex-col items-center">
              <div className="w-[123px] h-[103px] text-white font-[Poppins] text-[36px] leading-[24px] font-semibold flex items-center">
                Newzy
              </div>
              <div className="w-[100px] h-[60px] text-white font-[Poppins] text-[36px] leading-[24px] font-semibold flex items-center justify-center text-center">
                {user.newzy}
              </div>
            </div>
            <div className="flex flex-col items-center cursor-pointer" onClick={openModal}>
              <div className="w-[166px] h-[103px] text-white font-[Poppins] text-[36px] font-semibold flex items-center">
                Followers
              </div>
              <div className="w-[100px] h-[60px] text-white font-[Poppins] text-[36px] leading-[24px] font-semibold flex items-center justify-center text-center">
                {user.followers}
              </div>
            </div>
            <div className="flex flex-col items-center cursor-pointer" onClick={openModal}>
              <div className="w-[188px] h-[103px] text-white font-[Poppins] text-[36px] leading-[24px] font-semibold flex items-center">
                Followings
              </div>
              <div className="w-[100px] h-[60px] text-white font-[Poppins] text-[36px] leading-[24px] font-semibold flex items-center justify-center text-center">
                {user.followings}
              </div>
            </div>
          </div>

          {/* 프로필 편집 및 저장 버튼 */}
          {isEditing ? (
            <div className="w-[293px] h-[103px] relative mt-10 mb-7">
              <button
                className="w-full h-[73px] rounded-[10px] font-[Open Sans] bg-[#3578FF] hover:bg-[#2a61cc] flex items-center justify-center text-white text-[32px] font-semibold transition-colors duration-300"
                onClick={handleSave}
              >
                저장하기
              </button>
            </div>
          ) : (
            <div className="w-[293px] h-[103px] relative mt-10 mb-7">
              <button
                className="w-full h-[73px] rounded-[10px] font-[Open Sans] bg-[#3578FF] hover:bg-[#2a61cc] flex items-center justify-center text-white text-[32px] font-semibold transition-colors duration-300"
                onClick={() => setIsEditing(true)}
              >
                프로필 편집
              </button>
            </div>
          )}
        </div>
      </div>

      <MenuBar
        selectedMenu={selectedMenu}
        setSelectedMenu={handleMenuChange}
        menus={["My Newzy", "BookMark", "Words"]}
      />

      {renderContent()}

      <FollowIndexModal isOpen={isModalOpen} onClose={closeModal} />

      {/* 하단에 고정된 버튼 추가 */}
      <button
        className="fixed bottom-5 left-0.5 bg-transparent flex items-center justify-center cursor-pointer"
        style={{ zIndex: 1000 }}
        onClick={openCardListModal} // 버튼 클릭 시 CardListModal 열기
      >
        <img src={cards} alt="카드 버튼" className="w-full h-full object-cover" />
      </button>
      {/* CardListModal 렌더링 */}
      {isCardListModalOpen && <CardListModal onClose={closeCardListModal} cardNum={14}/>}
    </div>
  );
};