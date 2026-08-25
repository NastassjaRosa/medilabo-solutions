// Cree, au premier demarrage du conteneur mongo, un utilisateur applicatif
// limite a la base notesdb avec le seul role readWrite (moindre privilege).
// Le compte root (MONGO_INITDB_ROOT_*) ne sert qu'a l'initialisation du conteneur
// et n'est plus utilise par notes-service.
//
// Variables requises (voir .env.example) : MONGO_APP_USER, MONGO_APP_PASSWORD,
// MONGO_INITDB_DATABASE.

const appUser = process.env.MONGO_APP_USER;
const appPassword = process.env.MONGO_APP_PASSWORD;
const appDatabase = process.env.MONGO_INITDB_DATABASE;

if (!appUser || !appPassword || !appDatabase) {
  throw new Error('MONGO_APP_USER, MONGO_APP_PASSWORD et MONGO_INITDB_DATABASE doivent etre definies.');
}

db.getSiblingDB(appDatabase).createUser({
  user: appUser,
  pwd: appPassword,
  roles: [{ role: 'readWrite', db: appDatabase }],
});
