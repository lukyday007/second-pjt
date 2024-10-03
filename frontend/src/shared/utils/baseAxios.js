// src/shared/utils/baseAxios.js
import axios from "axios";

const BASE_SERVER_URL = "https://j11b305.p.ssafy.io/api";

function baseAxios() {
  const instance = axios.create({
    baseURL: BASE_SERVER_URL,
    headers: {
      "Content-Type": "application/json",
    },
    withCredentials: true,
  });

  // 요청 인터셉터 설정
  instance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("token"); // 로컬 스토리지에서 토큰 가져오기
      if (token) {
        config.headers.Authorization = `Bearer ${token}`; // 토큰이 있으면 Authorization 헤더에 추가
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  return instance;
}

export default baseAxios;