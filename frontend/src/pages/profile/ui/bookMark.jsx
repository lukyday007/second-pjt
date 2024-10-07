import React, { useState } from 'react';
import BookmarkNews from './bookMarkNews';
import BookmarkNewzy from './bookMarkNewzy';

const BookMark = () => {
  const [showNews, setShowNews] = useState(true); // 기본적으로 BookmarkNews를 보여줌

  return (
    <div className="px-10 py-5">
      <h1 className="text-2xl font-bold mb-4">Bookmark Page</h1>
      <div className="mb-4">
        <button 
          onClick={() => setShowNews(true)} // BookmarkNews 버튼
          className={`px-4 py-2 rounded ${showNews ? 'bg-blue-500 text-white' : 'bg-gray-300 text-black'}`}
        >
          News
        </button>
        <button 
          onClick={() => setShowNews(false)} // BookmarkNewzy 버튼
          className={`ml-2 px-4 py-2 rounded ${!showNews ? 'bg-blue-500 text-white' : 'bg-gray-300 text-black'}`}
        >
          Newzy
        </button>
      </div>
      {showNews ? <BookmarkNews /> : <BookmarkNewzy />}
    </div>
  );
};

export default BookMark;