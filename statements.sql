CREATE TABLE public."User"(
    "UserName" character varying(15) NOT NULL,
    CONSTRAINT User_pkey PRIMARY KEY ("UserName")
)

CREATE TABLE public."Candidate"(
    "UserName" character varying(15) NOT NULL,
    CONSTRAINT Candidate_pkey PRIMARY KEY ("UserName")
)

CREATE TABLE public."Tweet"(
    "TweetID" character varying(50) NOT NULL,
    text character varying(300) NOT NULL,
    "time" date NOT NULL,
    retweet_count integer,
    favourite_count integer,
    CONSTRAINT Tweet_pkey PRIMARY KEY ("TweetID")
)

CREATE TABLE public."Hashtag"(
    htext character varying(140) NOT NULL,
    CONSTRAINT Hashtag_pkey PRIMARY KEY (htext)
)

CREATE TABLE public.retweet_from(
    "UserName" character varying(15) NOT NULL,
    "TweetID" character varying(50) NOT NULL,
    CONSTRAINT retweet_from_pkey PRIMARY KEY ("UserName", "TweetID")
)

CREATE TABLE public.writes(
    "UserName" character varying(15) NOT NULL,
    "TweetID" character varying(50) NOT NULL,
    CONSTRAINT writes_pkey PRIMARY KEY ("UserName", "TweetID")
)

CREATE TABLE public.contains(
    "TweetID" character varying(50) NOT NULL,
    htext character varying(140) NOT NULL,
    CONSTRAINT contains_pkey PRIMARY KEY ("TweetID", htext)
)