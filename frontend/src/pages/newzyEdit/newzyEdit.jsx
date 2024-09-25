import React, { useState } from 'react';
import TitleInput from './ui/titleInput';
import CategorySelector from './ui/categorySelector';
import ContentEditor from './ui/contentEditor';

export const NewzyEdit = () => {
  const [formData, setFormData] = useState({
    title: '',
    category: '시사',
    content: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({ ...prevData, [name]: value }));
  };

  const handleCategoryChange = (category) => {
    setFormData((prevData) => ({ ...prevData, category }));
  };

  const handleSave = () => {
    if (!formData.title || !formData.category || !formData.content) {
      alert('모든 내용을 채워주세요.');
      return;
    }
    console.log(formData);
  };  

  return (
    <div className="bg-white">
      {/* 헤더 - 저장 버튼 */}
      <div className="w-full h-[50px] border-b border-gray-200 shadow relative"> 
        <button
          onClick={handleSave}
          className="w-[80px] h-[30px] bg-[#565656] text-white text-[20px] font-bold rounded-md absolute top-[10px] right-0 mr-4"
        >
          저장
        </button>
      </div>

      {/* 본문 */}
      <div className="flex h-screen">
        {/* 왼쪽 사이드바 */}
        <div className="w-[15%]"></div>

        <div className="flex-1 p-6">
          <div className="mb-3">
            <TitleInput title={formData.title} onChange={handleChange} />
          </div>

          <div className="mb-8">
            <CategorySelector category={formData.category} onCategoryChange={handleCategoryChange} />
          </div>

          <ContentEditor content={formData.content} setContent={(content) => setFormData((prevData) => ({ ...prevData, content }))} />
        </div>

        {/* 오른쪽 사이드바 */}
        <div className="w-[15%] relative"></div>
      </div>
    </div>
  );
};