import React, {useEffect, useRef, useState} from 'react';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';

import {faCheck, faTimes, faInfoCircle} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import logo from '../assets/ROLIP_Logo.jpg'
import axios from '../api/axios.js'
import useAuth from '../auth/useAuth';
import { useNavigate, useLocation } from 'react-router-dom';


const LOGIN_URL = 'auth/login';
const SIGNUP_URL = 'auth/signup';

const EMAIL_REGEX = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/;
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;


const SignupForm = () => {
  const { setAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || "/";

  const userRef = useRef();
  const [isSignUp, setIsSignUp] = useState(false);

  const [userName, setUserName] = useState('');
  const [availabledUser, setAvailabledUser] = useState(false);

  const [mail, setMail] = useState('');
  const [validMail, setValidMale] = useState(false);

  const [pwd, setPwd] = useState('');
  const [validPwd, setValidPwd] = useState(false);
  const [pwdFocus, setPwdFocus] = useState(false);

  const [matchPwd, setMatchPwd] = useState('');
  const [validMatch, setValidMatch] = useState(false);
  const [matchFocus, setMatchFocus] = useState(false);

  const [errMsg, setErrMsg] = useState('');

  //page load hock
  useEffect(() => {
    userRef.current.focus();
  }, [])

  //check username
useEffect(() => {
  let cooldownTimer = null;

  const checkUsernameAvailability = async () => {
    if (userName.length < 3) {
        setAvailabledUser(false);
        return;
    }
    //request username availability on server
    try {
      const { status } = await axios.get('/auth/signup/username', {
        params: { username: userName },
      });

      if (status === 200) {
        setAvailabledUser(true);
      }
    } catch ({ response }) {
      if (response?.status === 409) {
        setAvailabledUser(false);
      } else {
        setErrMsg('Server error, please try again later');
        console.error('An unexpected error occurred:', response?.statusText || 'Unknown error');
      }
    }
  };

  if (userName) {
    cooldownTimer = setTimeout(() => {
      checkUsernameAvailability();
    }, 1000); // 1000ms cooldown
  }

  return () => {
    if (cooldownTimer) {
      clearTimeout(cooldownTimer);
    }
  };
}, [userName]);

  //email filed changed hook
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
      let response = null;
      if (isSignUp) {
        response = await axios.post(
            SIGNUP_URL,
            {
              username: userName,
              email: mail,
              password: pwd
            },
            {
              headers: {'Content-Type': 'application/json'},
              withCredentials: true
            }
        );
      }

      // login even necesery after signup to get the access token!
      response = await axios.post(
          LOGIN_URL,
          {
            email: mail,
            password: pwd
          },
          {
            headers: {'Content-Type': 'application/json'},
            withCredentials: true
          });


      const accessToken = response?.data?.token;
      const authMail = response?.data?.email;
      const expiresIn = response?.data?.expiresIn;
      const role = response?.data?.role;
      setAuth({authMail, accessToken, expiresIn, role});

      setMail('');
      setPwd('');
      setMatchPwd('');
      navigate(from, { replace: true }); //links back from where you come from!

    } catch (err) {
      if (!err?.response) {
        setErrMsg('No Server Response');
      } else if (err.response?.status === 409) {
        setErrMsg('E-Mail is already claimed');
      } else {
        if(err?.response?.data?.errorMessage){
            setErrMsg(err?.response?.data?.errorMessage);
        }else {
          setErrMsg('Registration railed for unknowen reason')
        }
      }
      //errRef.current.focus();
    }

  }

  const toggleSignUp = () => {
    setIsSignUp(!isSignUp);
  }

  return (
        <Container className="d-flex align-items-center justify-content-center vh-100">
          <Row className="justify-content-md-center border rounded bg-primary p-4 text-white">
            <Col className="align-items-center" style={{width: '300px'}}>
                <img src={logo} alt="ROLIP Logo" className="mb-3 img-fluid"  style={{height:'auto'}}/>
                <h2>{isSignUp ? 'Signup' : 'Login'}</h2>
                <Form onSubmit={handleSubmit}>
                  {isSignUp && (
                    <>
                      <label htmlFor="user_name" className="form-label">
                        Username:
                      </label>
                      <input
                        type="text"
                        id="user_name"
                        onChange={(e) => setUserName(e.target.value)}
                        value={userName}
                        required
                        className="form-control"
                        placeholder="Enter your user name"
                      />
                      <span className={availabledUser ? "text-success position-relative" : "d-none"} style={{ top: '-30px', right: '-275px' }}>
                        <FontAwesomeIcon icon={faCheck} />
                      </span>
                      <span className={availabledUser || userName.length <= 3  ? "d-none" : "text-danger position-relative"} style={{ top: '-30px', right: '-275px' }}>
                        <FontAwesomeIcon icon={faTimes} />
                      </span>
                    </>
                  )}


                  <label htmlFor="email" className="form-label">
                    E-Mail:
                  </label>
                  <input
                      type="text"
                      id="email"
                      ref={userRef}
                      autoComplete="off"
                      onChange={(e) => setMail(e.target.value)}
                      value={mail}
                      required
                      aria-invalid={validMail ? "false" : "true"}
                      aria-describedby="uidnote"
                      placeholder="Enter your email"
                      className="form-control d-inline-flex"
                  />
                  <span className={validMail ? "text-success position-relative" : "d-none"} style={{ top: '-30px', right: '-275px' }}>
                    <FontAwesomeIcon icon={faCheck} />
                  </span>
                  <span className={validMail || !mail ? "d-none" : "text-danger position-relative"} style={{ top: '-30px', right: '-275px' }}>
                    <FontAwesomeIcon icon={faTimes} />
                  </span>
                  <label htmlFor="password" className="form-label">Password:
                  </label>
                  <input type="password" id="password" onChange={(e) => setPwd(e.target.value)} value={pwd} required aria-invalid={validPwd ? "false" : "true"} aria-describedby="pwdnote" onFocus={() => setPwdFocus(true)} onBlur={() => setPwdFocus(false)} placeholder="Enter your Password" className="form-control"
                  />

                  {isSignUp && (
                      <>
                        <p id="pwdnote" className={pwdFocus && !validPwd ? "text-danger" : "d-none"}>
                          <FontAwesomeIcon icon={faInfoCircle}/> 8 to 24 characters.<br />
                          Must include uppercase and lowercase letters, a number and a special character.<br />
                          Allowed special characters: <span aria-label="exclamation mark">!</span> <span aria-label="at symbol">@</span> <span aria-label="hashtag">#</span> <span aria-label="dollar sign">$</span> <span aria-label="percent">%</span>
                        </p>
                        <label htmlFor="confirm_pwd" className="form-label">
                          Confirm Password:
                        </label>
                        <input
                            type="password"
                            id="confirm_pwd"
                            onChange={(e) => setMatchPwd(e.target.value)}
                            value={matchPwd}
                            required
                            aria-invalid={validMatch ? "false" : "true"}
                            aria-describedby="confirmnote"
                            onFocus={() => setMatchFocus(true)}
                            onBlur={() => setMatchFocus(false)}
                            className="form-control"
                            placeholder={"Confirm your Password"}
                        />
                        <p id="confirmnote" className={matchFocus && !validMatch ? "text-danger" : "d-none"}>
                          <FontAwesomeIcon icon={faInfoCircle}/> Must match the first password input field.
                        </p>
                      </>
                  )}

                  <br/>
                    <Button type={'submit'} disabled={!validMail || !validPwd || (isSignUp && !validMatch)} className="btn-secondary">
                      {isSignUp ? 'Sign Up' : 'Login'}
                    </Button>
                </Form>
              <div className={"d-flex justify-content-center"}>
                <Button variant="link" onClick={toggleSignUp} className="text-white ">
                  {isSignUp ? 'Already have an account? Login': 'Create an account'}
                </Button>
              </div>
              <p className={ errMsg ? 'text-danger': 'd-none'}>
                {errMsg}
              </p>
            </Col>
          </Row>
        </Container>
  );
};

export default SignupForm;