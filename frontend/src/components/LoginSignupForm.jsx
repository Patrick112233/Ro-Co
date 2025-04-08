import React, {useEffect, useState} from 'react';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import {faCheck, faTimes, faInfoCircle} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import '../bootstrap.min.css';
import logo from '../assets/ROLIP_Logo.jpg'
import { useNavigate } from 'react-router-dom';

const EMAIL_REGEX = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/;
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;


const LoginSignupForm = () => {
  const navigate = useNavigate();
  const userRef = useRef();
  const errRef = useRef();

  const [mail, setMail] = useState('');
  const [validMail, setValidMale] = useState(false);
  const [mailFocus, setMailFocus] = useState(false);

  const [pwd, setPwd] = useState('');
  const [validPwd, setValidPwd] = useState(false);
  const [pwdFocus, setPwdFocus] = useState(false);

  const [matchPwd, setMatchPwd] = useState('');
  const [validMatch, setValidMatch] = useState(false);
  const [matchFocus, setMatchFocus] = useState(false);

  const [errMsg, setErrMsg] = useState('');
  const [success, setSuccess] = useState(false);

  //page load hock
  useEffect(() => {
    userRef.current.focus();
  }, [])

  //user filed changed hook
  useEffect(() => {
    setValidMale(EMAIL_REGEX.test(mail));
  }, [mail])

  //pwd filed changed hook (checks if pwds matchs and if pwd is valid)
  useEffect(() => {
    setValidPwd(PWD_REGEX.test(pwd));
    setValidMatch(pwd === matchPwd);
  }, [pwd, matchPwd])

  //hide error msg on any change
  useEffect(() => {
    setErrMsg('');
  }, [mail, pwd, matchPwd])

  const handleSubmit = async (e) => {
    e.preventDefault();
    // if button enabled with JS hack
    const v1 = EMAIL_REGEX.test(mail);
    const v2 = PWD_REGEX.test(pwd);
    if (!v1 || !v2) {
      setErrMsg("Invalid Entry");
      return;
    }
    try {
      const response = await axios.post(REGISTER_URL,
          JSON.stringify({ mail, pwd }),
          {
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true
          }
      );
      console.log(response?.data);
      console.log(response?.accessToken);
      console.log(JSON.stringify(response))
      setSuccess(true);
      //clear state and controlled inputs
      //need value attrib on inputs for this
      setMail('');
      setPwd('');
      setMatchPwd('');

      //redirect
      //navigate('/dashboard');
    } catch (err) {
      if (!err?.response) {
        setErrMsg('No Server Response');
      } else if (err.response?.status === 409) {
        setErrMsg('Username Taken');
      } else {
        setErrMsg('Registration Failed')
      }
      errRef.current.focus();
    }

  }



  const [isLogin, setIsLogin] = useState(true);

  const toggleForm = () => {
    setIsLogin(!isLogin);
  };

  return (
      <div className="p-4 border rounded bg-primary text-white" style={{ Width: '400px' }}>
        <img src={logo} alt="ROLIP Logo" className="mb-3 img-fluid"  />
        <h2>{isLogin ? 'Login' : 'Signup'}</h2>
        <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>

        <Form>
          <label htmlFor="username">
            Username:
            <FontAwesomeIcon icon={faCheck} className={validName ? "valid" : "hide"} />
            <FontAwesomeIcon icon={faTimes} className={validName || !user ? "hide" : "invalid"} />
          </label>
          <input
              type="text"
              id="username"
              ref={userRef}
              autoComplete="off"
              onChange={(e) => setUser(e.target.value)}
              value={user}
              required
              aria-invalid={validName ? "false" : "true"}
              aria-describedby="uidnote"
              onFocus={() => setUserFocus(true)}
              onBlur={() => setUserFocus(false)}
          />


        </Form>
  );
};

export default LoginSignupForm;