import React from 'react';

const SearchBar = ({ selectedRange, onRangeClick, searchTerm, onSearchChange, onClearSearch, onKeyPress }) => {
  return (
    <div className="relative flex items-center">
      <button
        className={`mr-2 px-4 py-2 rounded ${selectedRange === '전체' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
        onClick={() => onRangeClick('전체')}
      >
        전체
      </button>
      <button
        className={`mr-2 px-4 py-2 rounded ${selectedRange === '구독' ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-700'}`}
        onClick={() => onRangeClick('구독')}
      >
        구독
      </button>
      <input
        type="text"
        value={searchTerm}
        onChange={onSearchChange}
        onKeyPress={onKeyPress}
        placeholder="검색..."
        className="border p-2 rounded pr-10"
      />
      {searchTerm && (
        <button
          onClick={onClearSearch}
          className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500"
        >
          &times;
        </button>
      )}
    </div>
  );
};

export default SearchBar;