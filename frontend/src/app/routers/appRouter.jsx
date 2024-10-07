import {
  createBrowserRouter,
  createRoutesFromElements,
  RouterProvider,
  Route,
  Navigate,
} from "react-router-dom";

import { Layout } from "app/layout/layout";
import { Home } from "pages/home";
import { NewsList } from "pages/newsList";
import { NewzyList } from "pages/newzyList";
import { Profile } from "pages/profile";
import { AnotherProfile } from "pages/profile";
import { NewsDetail } from "../../pages/newsDetail/newsDetail";
import { NewzyEdit } from "../../pages/newzyEdit/newzyEdit";
import { NewzyDetail } from "../../pages/newzyDetail/newzyDetail";
import { UserTest } from "pages/userTest";

export const AppRouter = () => {
  const routers = createRoutesFromElements(
    <Route path="/" element={<Layout />}>
      <Route index element={<Home />} />
      <Route path="news">
        <Route index element={<NewsList />} />
        <Route path=":id" element={<NewsDetail />} />
        <Route path="economy" element={<NewsList category="economy" />} />
        <Route path="social" element={<NewsList category="social" />} />
        <Route path="global" element={<NewsList category="global" />} />
      </Route>
      <Route path="newzy">
        <Route index element={<NewzyList />} />
        {/* 새 글 작성 라우트 */}
        <Route path="edit" element={<NewzyEdit />} />
        {/* 수정 라우트 */}
        <Route path="edit/:newzyId" element={<NewzyEdit />} />
        <Route path=":id" element={<NewzyDetail />} />
      </Route>
      <Route path="profile">
        <Route index element={<Navigate to="myNewzy" replace />} />
        <Route path="myNewzy" element={<Profile />} />
        <Route path="bookMark" element={<Profile />} />
        <Route path="words" element={<Profile />} />
      </Route>
      <Route path="profile/:nickname" element={<AnotherProfile />} />
      <Route path="usertest" element={<UserTest />} />
    </Route>
  );

  const router = createBrowserRouter(routers, {});
  return (
    <div>
      <RouterProvider router={router} />
    </div>
  );
};