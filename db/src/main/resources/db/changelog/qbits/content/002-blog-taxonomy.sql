--liquibase formatted sql
--changeset qrun:content-002-blog-taxonomy
CREATE TABLE cms_blog_post_category (blog_post_id uuid NOT NULL REFERENCES cms_blog_post(id) ON DELETE CASCADE,category_id uuid NOT NULL REFERENCES cms_category(id) ON DELETE CASCADE,PRIMARY KEY(blog_post_id,category_id));
CREATE TABLE cms_blog_post_tag (blog_post_id uuid NOT NULL REFERENCES cms_blog_post(id) ON DELETE CASCADE,tag_id uuid NOT NULL REFERENCES cms_tag(id) ON DELETE CASCADE,PRIMARY KEY(blog_post_id,tag_id));
--rollback DROP TABLE cms_blog_post_tag,cms_blog_post_category;
