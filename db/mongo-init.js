//use admin

const root = process.env.MONGO_INITDB_ROOT_USERNAME;
const pwd = process.env.MONGO_INITDB_ROOT_PASSWORD;
const certificate_string = process.env.MONGO_ROCO_CERT_STRING; /*"CN=RoCoClient,OU=RoCoUser,O=RoCo,L=Rosenheim,ST=Bavaria,C=myCountry" */

db = db.getSiblingDB("RoCoDB");
// create admin user if vars are set:
if (root && pwd) {
    db.createUser({
        user: root,
        pwd: pwd,
        roles: [{ role: "readWrite", db: "RoCoDB" }, { role: "userAdminAnyDatabase", db: "admin" }]
    });
}

//define user role
db.createRole(
   {
     role: "RoCoUser", 
     privileges: [
       {
         actions: [ "changeStream", "createCollection", "createIndex", "createSearchIndex", "dbHash", "dropIndex", "dropSearchIndex", "find", "insert", "killCursors", "listCollections", "listIndexes", "listSearchIndexes", "remove", "update","updateSearchIndex" ],
         resource: { db: "RoCoDB", collection: "" }
       }
     ],
     roles: []
   }
)


// create user authenticated for RoCO Rest API via x.509 certificate
// see https://www.mongodb.com/docs/manual/core/security-x.509/
db.getSiblingDB("$external").runCommand(
  {
    createUser: certificate_string,
    roles: [
         { role: "RoCoUser", db: "RoCoDB" }
    ],
    writeConcern: { w: "majority" , wtimeout: 5000 }
  }
)
print("User created in RoCoDB.");