ALTER TABLE "candidatos"
    ADD COLUMN "id_estado" int REFERENCES "estados" ("id");
