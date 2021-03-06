SELECT
  AUTHID AS USERNAME,
  BINDADDAUTH,
  CONNECTAUTH,
  CREATETABAUTH,
  DBADMAUTH,
  EXTERNALROUTINEAUTH,
  IMPLSCHEMAAUTH,
  LOADAUTH,
  NOFENCEAUTH,
  QUIESCECONNECTAUTH,
  SECURITYADMAUTH,
  SQLADMAUTH,
  WLMADMAUTH,
  EXPLAINAUTH,
  DATAACCESSAUTH,
  ACCESSCTRLAUTH,
  CREATESECUREAUTH
FROM
  SYSIBMADM.AUTHORIZATIONIDS A
  LEFT JOIN SYSCAT.DBAUTH D
    ON D.GRANTEE = A.AUTHID
WHERE
  AUTHIDTYPE = 'U'
WITH UR
