
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import NavBar from './components/NavBar';
import Login from './pages/Login';
import Members from './pages/Members';
import Events from './pages/Events';
import EventDetail from './pages/EventDetail';
import FinanceDashboard from './pages/FinanceDashboard';
import Posts from './pages/Posts';
import Uploads from './pages/Uploads';
import Gallery from './pages/Gallery';
import GalleryAdmin from './pages/GalleryAdmin';
import AlbumManager from './pages/AlbumManager';
import Mission from './pages/Mission';
import Contact from './pages/Contact';
import ChangePassword from './pages/ChangePassword';
import About from './pages/About';
import { useAuth } from './context/AuthContext';
import AdminPageEditor from './pages/AdminPageEditor';
import AdminRegister from './pages/AdminRegister';
function PrivateRoute({ children }){ const { user }=useAuth(); return user ? children : <Navigate to="/login"/> }
export default function App(){ return (
  <BrowserRouter>
    <NavBar/>
    <Routes>
      <Route path="/login" element={<Login/>}/>
      <Route path="/members" element={<PrivateRoute><Members/></PrivateRoute>}/>
      <Route path="/events" element={<PrivateRoute><Events/></PrivateRoute>}/>
      <Route path="/events/:id" element={<PrivateRoute><EventDetail/></PrivateRoute>}/>
      <Route path="/finance" element={<PrivateRoute><FinanceDashboard/></PrivateRoute>}/>
      <Route path="/posts" element={<PrivateRoute><Posts/></PrivateRoute>}/>
      <Route path="/uploads" element={<PrivateRoute><Uploads/></PrivateRoute>}/>
      <Route path="/gallery" element={<Gallery/>} />
      <Route path="/mission" element={<Mission/>}/>
      <Route path="/contact" element={<Contact/>}/>
      <Route path="/change-password" element={<PrivateRoute><ChangePassword/></PrivateRoute>}/>
      <Route path="/about" element={<About/>}/>
      <Route path="*" element={<Navigate to="/about"/>} />
      <Route path="/admin/gallery" element={<PrivateRoute><GalleryAdmin/></PrivateRoute>} />
      <Route path="/albums/:albumId" element={<PrivateRoute><AlbumManager/></PrivateRoute>} />
      <Route path="/admin/pages" element={<PrivateRoute><AdminPageEditor/></PrivateRoute>} />
      <Route path="/admin/register" element={<PrivateRoute><AdminRegister/></PrivateRoute>} />
    </Routes>
  </BrowserRouter> ); }
