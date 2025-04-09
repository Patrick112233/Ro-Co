import React, {useEffect, useRef, useState, useContext} from 'react';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';
import {faCheck, faTimes, faInfoCircle} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import '../bootstrap.min.css';
import logo from '../assets/ROLIP_Logo.jpg'
import {data, useNavigate} from 'react-router-dom';
import AuthContext from '../context/AuthProvider.jsx';
import axios from '../api/axios.js'
import * as https from "node:https";

const LOGIN_URL = '/auth/login';
const SIGNUP_URL = '/auth/signup';

const EMAIL_REGEX = /^((?!\.)[\w\-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/;
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;


const SignupForm = () => {
  const {setAuth} = useContext(AuthContext);
  const navigate = useNavigate();
  const userRef = useRef();
  const errRef = useRef();
  const [isSignUp, setIsSignUp] = useState(false);

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

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

      //let body
      //if (isSignUp){
      let body = JSON.stringify({
          "firstName": firstName,
          "lastName": lastName,
          "email": mail,
          "password": pwd
        });
      /*}else {
        body = JSON.stringify({
          "email": mail,
          "password": pwd
        });
      }*/

      const response = await axios.post(
          isSignUp?SIGNUP_URL:LOGIN_URL,
          body,
          {
            method: 'post',
            maxBodyLength: Infinity,
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true,
            httpsAgent: new https.Agent({
                rejectUnauthorized: false
            })
          }
          );

      const accessToken = response?.data?.token;
      const authMail = response?.data?.token;
      setAuth({authMail, accessToken});

      console.log(response?.data);
      console.log(response?.accessToken);
      console.log(JSON.stringify(response))
      setSuccess(true);
      //clear state and controlled inputs
      //need value attrib on inputs for this
      setMail('');
      setPwd('');
      setMatchPwd('');

    } catch (err) {
      if (!err?.response) {
        setErrMsg('No Server Response');
      } else if (err.response?.status === 409) {
        setErrMsg('E-Mail is already claimed');
      } else {
        setErrMsg('Registration Failed')
      }
      //errRef.current.focus();
    }

  }

  const toggleSignUp = () => {
    setIsSignUp(!isSignUp);
  }

  return (
      <>
        {success ? (
            navigate('/dashboard')
        ) : (
        <Container className="d-flex align-items-center justify-content-center vh-100">
          <Row className="justify-content-md-center border rounded bg-primary p-4 text-white">
            <Col className="align-items-center" style={{width: '300px'}}>
                <img src={logo} alt="ROLIP Logo" className="mb-3 img-fluid"  style={{height:'auto'}}/>
                <h2>{isSignUp ? 'Signup' : 'Login'}</h2>
                <Form onSubmit={handleSubmit}>
                  {isSignUp && (
                    <>
                      <label htmlFor="first_name" className="form-label">
                        First Name:
                      </label>
                      <input
                        type="text"
                        id="first_name"
                        onChange={(e) => setFirstName(e.target.value)}
                        value={firstName}
                        required
                        className="form-control"
                        placeholder="Enter your first name"
                      />
                      <label htmlFor="last_name" className="form-label">
                        Last Name:
                      </label>
                      <input
                        type="text"
                        id="last_name"
                        onChange={(e) => setLastName(e.target.value)}
                        value={lastName}
                        required
                        className="form-control"
                        placeholder="Enter your last name"
                      />
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
                      onFocus={() => setMailFocus(true)}
                      onBlur={() => setMailFocus(false)}
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
        )};
      </>

  );
};

export default SignupForm;