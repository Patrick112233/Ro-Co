import React from 'react';
import { Container, Row, Col, Card, ListGroup, Image } from 'react-bootstrap';

const Dashboard = () => {
  const events = [
    { date: '2023-12-01', participants: 10, description: 'Event 1' },
    { date: '2023-12-05', participants: 20, description: 'Event 2' },
    { date: '2023-12-10', participants: 15, description: 'Event 3' },
  ];

  const questions = [
    { user: 'User1', profilePic: 'path/to/profile1.jpg', headline: 'Question 1', body: 'This is the first question.' },
    { user: 'User2', profilePic: 'path/to/profile2.jpg', headline: 'Question 2', body: 'This is the second question.' },
    { user: 'User3', profilePic: 'path/to/profile3.jpg', headline: 'Question 3', body: 'This is the third question.' },
  ];

  return (
    <Container fluid>
      <Row className="mt-4">
        <Col md={3}>
          <h4>Upcoming Events</h4>
          <div style={{ overflowX: 'auto', whiteSpace: 'nowrap' }}>
            {events.map((event, index) => (
              <Card key={index} className="d-inline-block mr-2" style={{ width: '200px' }}>
                <Card.Body>
                  <Card.Title>{event.date}</Card.Title>
                  <Card.Text>Participants: {event.participants}</Card.Text>
                  <Card.Text>{event.description}</Card.Text>
                </Card.Body>
              </Card>
            ))}
          </div>
        </Col>
        <Col md={9}>
          <h4>Questions</h4>
          <ListGroup>
            {questions.map((question, index) => (
              <ListGroup.Item key={index} className="mb-3">
                <Row>
                  <Col md={2}>
                    <Image src={question.profilePic} roundedCircle fluid />
                  </Col>
                  <Col md={10}>
                    <h5>{question.headline}</h5>
                    <p>{question.body}</p>
                  </Col>
                </Row>
              </ListGroup.Item>
            ))}
          </ListGroup>
        </Col>
      </Row>
    </Container>
  );
};

export default Dashboard;