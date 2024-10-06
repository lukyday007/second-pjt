import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import NewsInfo from './ui/newsInfo';
import Content from '../../shared/postDetail/content';
import UtilityButtons from './ui/utilityButtons';
import Sidebar from '../../shared/postDetail/sidebar';

export const NewsDetail = () => {
  const [activeSidebar, setActiveSidebar] = useState(null);
  const [news, setNews] = useState(null);

  const { id } = useParams();

  const handleSidebarToggle = (type) => {
    setActiveSidebar((prev) => (prev === type ? null : type));
  };

  useEffect(() => {
    window.scrollTo(0, 0);
    fetchNews();
  }, []);

  useEffect(() => {
    if (activeSidebar) {
      window.scrollTo(0, 0);
    }
  }, [activeSidebar]);

  const fetchNews = async () => {
    try {
      const response = await axios.get(`https://j11b305.p.ssafy.io/api/news/${id}`);
      setNews(response.data);
    } catch (error) {
      console.error("Error fetching news details:", error);
    }
  };

  const htmlContent = news ? news.content : "";

  return (
    <div className="relative flex h-screen bg-white">
      <div className="w-[17%]"></div>

      <div className="flex-1 p-6">
        {news && (
          <NewsInfo 
            category={getCategoryName(news.category)}
            title={news.title} 
            date={new Date(news.createdAt).toLocaleString('ko-KR')}
            publisher={news.publisher}
          />
        )}
        <Content htmlContent={htmlContent} />
      </div>

      <div className="w-[17%]"></div>

      <UtilityButtons onActiveSidebar={handleSidebarToggle} activeSidebar={activeSidebar} />
      <Sidebar activeSidebar={activeSidebar} onActiveSidebar={handleSidebarToggle} />
    </div>
  );
};

const getCategoryName = (category) => {
  switch (category) {
    case 0:
      return "경제";
    case 1:
      return "사회";
    case 2:
      return "세계";
    default:
      return "";
  }
};