import React, {useEffect, useRef, useState} from 'react';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import useSignIn from 'react-auth-kit/hooks/useSignIn';
import {faInfoCircle} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import logo from '../assets/ROLIP_Logo.jpg'
import axios from '../api/axios.js'
import { useLocation, useNavigate } from 'react-router-dom';


const LOGIN_URL = 'auth/login';
const SIGNUP_URL = 'auth/signup';

const EMAIL_REGEX = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/;
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;


const SignupForm = () => {
  const signIn = useSignIn()


  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || "/";

  const userRef = useRef();
  const [isSignUp, setIsSignUp] = useState(false);

  const [userName, setUserName] = useState('');
  const [isUsernameAvailable, setIsUsernameAvailable] = useState(false);

  const [mail, setMail] = useState('');
  const [validMail, setValidMale] = useState(false);

  const [pwd, setPwd] = useState('');
  const [validPwd, setValidPwd] = useState(false);

  const [matchPwd, setMatchPwd] = useState('');
  const [validMatch, setValidMatch] = useState(false);

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
        setIsUsernameAvailable(false);
        return;
    }
    //request username availability on server
    try {
      const { status } = await axios.get('/auth/signup/username', {
        params: { username: userName },
      });

      if (status === 200) {
        setIsUsernameAvailable(true);
      }
    } catch ({ response }) {
      if (response?.status === 409) {
        setIsUsernameAvailable(false);
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
      const refreshToken = response?.data?.refresh; //@TODO: impl refresh token in backend!
      const authMail = response?.data?.email;
      const role = response?.data?.role;


      if(!signIn({
        auth: {
          token: accessToken,
          type: 'Bearer'
        },
        refresh: refreshToken,
        userState: {
          name: authMail,
          role: role,
          uid: 123456 //@TODO: Get user ID ID
        }
      })){ // on Error
          setErrMsg('Login failed');
          return;
      }

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
                        <div className="input-group has-validation d-block">
                          <input
                              type="text"
                              id="user_name"
                              onChange={(e) => setUserName(e.target.value)}
                              className={userName.length <= 3 ? "form-control w-100" : (isUsernameAvailable ? "form-control is-valid w-100" : "form-control is-invalid w-100")}
                              placeholder="Enter your user name"
                              value={userName}
                              required
                          />

                          {!isUsernameAvailable && userName.length > 3 && (
                              <div className="text-danger">
                                Username is already taken!
                              </div>
                          )}
                        </div>
                      </>
                  )}


                  <label htmlFor="email" className="form-label">
                    E-Mail:
                  </label>
                  <div className="input-group has-validation">
                    <span className="input-group-text" id="inputGroupPrepend3">@</span>
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
                      className={mail.length <= 0 ?  "form-control d-inline-flex": validMail ? "form-control d-inline-flex is-valid" : "form-control d-inline-flex is-invalid"}

                  />
                  </div>


                  <label htmlFor="password" className="form-label">Password:
                  </label>
                  <input
                      type="password"
                      id="password"
                      onChange={(e) => setPwd(e.target.value)}
                      value={pwd}
                      required
                      aria-invalid={validPwd ? "false" : "true"}
                      aria-describedby="pwdnote"
                      placeholder="Enter your Password"
                      className={!isSignUp || pwd.length <= 0 ? "form-control" : (validPwd ? "form-control is-valid" : "form-control is-invalid")}
                          />
                  { !validPwd && pwd.length > 0 && isSignUp && (
                      <div className="text-danger">
                          Must uses 8 to 24 characters.
                          Must include uppercase and lowercase letters, a number and a special character.<br />
                          Allowed special characters: <span aria-label="exclamation mark">!</span> <span aria-label="at symbol">@</span> <span aria-label="hashtag">#</span> <span aria-label="dollar sign">$</span> <span aria-label="percent">%</span>
                      </div>
                  )}


                  {isSignUp && (
                      <>
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
                              className={validMatch ? "form-control is-valid" : "form-control is-invalid"}
                              placeholder={"Confirm your Password"}
                          />
                          {!validMatch &&(
                              <div className="text-danger">
                                <FontAwesomeIcon icon={faInfoCircle}/> Must match the first password input field.
                              </div>
                          )}
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