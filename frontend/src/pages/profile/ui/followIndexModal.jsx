/* eslint-disable react/prop-types */
import { FaUserCircle } from "react-icons/fa";
import { MdCancel } from "react-icons/md";
import { useState, useEffect } from "react";
import FollowListItem from "./followListItem";
import baseAxios from "shared/utils/baseAxios";
import { useFollowStore } from "../store/useFollowStore";
import useAuthStore from "shared/store/userStore";

const FollowIndexModal = ({ isOpen, onClose, userInfo, followerCnt, followingCnt }) => {

  const [selectedMenu, setSelectedMenu] = useState(0);  // 메뉴 상태
  const { followers, followings, updateFollowStatus, fetchFollowers, fetchFollowings } = useFollowStore();
  const { userInfo: loggedInUser, fetchFollowers: loggedInUserFollowers, fetchFollowings: loggedInUserFollowings, followings: loggedInUserFollowingsIndex } = useAuthStore();  // ✅ 로그인된 사용자 정보

  if (!isOpen) return null;

  // 메뉴 배열 생성 (팔로워와 팔로잉 목록을 선택할 수 있도록)
  const menus = [`팔로워 ${followerCnt}명`, `팔로잉 ${followingCnt}명`];
  console.log(followers, '팔로워 목록') 
  console.log(followings, '팔로잉 목록') 

  // 선택된 메뉴에 따라 팔로워 또는 팔로잉 목록을 렌더링하는 함수
  const renderContent = () => {
    if (selectedMenu === 0) {
      // 팔로워 목록 렌더링
      return followers.map((follower) => (
        <div key={follower.fromUserNickname} className="relative py-1 px-5 flex flex-col">
          <FollowListItem
            PageOwner={userInfo} 
            name={follower.fromUserNickname} 
            isFollowing={loggedInUserFollowingsIndex.some(
              (following) => following.toUserNickname === follower.fromUserNickname
            )}  // 팔로우 여부 확인
          />
        </div>
      ));
    } else {
      // 팔로잉 목록 렌더링
      return followings.map((following) => (
        <div key={following.toUserNickname} className="relative py-1 px-5 flex flex-col">
          <FollowListItem
            PageOwner={userInfo} 
            name={following.toUserNickname} 
            isFollowing={loggedInUserFollowingsIndex.some(
              (following_2) => following_2.toUserNickname === following.toUserNickname
            )}  // 팔로우 여부 확인
          />
        </div>
      ));
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="w-[480px] h-[650px] transform rotate-[0.5deg] rounded-[24px] bg-white border-[3px] border-gray-400 px-6">
        <header className="w-full flex justify-between items-center mt-4">
          <div className="flex-shrink-0 text-[#26262C] whitespace-nowrap text-ellipsis font-[Open Sans] text-[36px] leading-[24px] font-semibold flex items-center justify-between text-justify">
            <button className="w-[45px] h-[45px] bg-gray-200 rounded-full">
              <img src={userInfo.profile || "/shared/images/user.png"} className="w-full h-full object-cover rounded-full" />
            </button>
          </div>
          <div className="flex items-center">
            <button className="flex-shrink-0 w-[37px] h-[37px] flex items-center justify-center rounded-full hover:bg-white bg-opacity-30">
              <MdCancel className="w-10 h-10 text-red-600" onClick={onClose}/>
            </button>
          </div>
        </header>

        {/* MenuBar에 선택된 메뉴 상태와 상태 변경 함수 전달 */}
        <div className="flex items-center justify-center border-b-2 border-gray-300 font-semibold text-[18px]">
        {menus.map((menu, index) => (
          <div 
            key={index}
            className="relative inline-block cursor-pointer py-2 px-10"
            onClick={() => setSelectedMenu(index)} // 메뉴 클릭 시 선택 상태 변경
          >
            <span className={`${selectedMenu === index ? 'text-blue-500' : 'text-gray-400'} transition-all duration-300`}>
              {menu}
            </span>
            {/* 선택된 메뉴의 하단에 파란색 라인 */}
            <div className={`absolute bottom-[-3px] left-0 right-0 h-1 
              ${selectedMenu === index ? 'bg-blue-500' : 'bg-transparent'} 
              transition-all duration-300`}></div>
          </div>
          ))}
        </div>

      <div className="h-[520px] overflow-y-auto">
        {/* 선택된 메뉴에 따라 다른 컴포넌트 렌더링 */}
        {renderContent()}
      </div>
      </div>
    </div>
  );
};

export default FollowIndexModal;
