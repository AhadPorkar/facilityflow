-- PostgreSQL is the source of truth: managed schema, explicit foreign keys and indexes.
CREATE TABLE organization (
  id uuid PRIMARY KEY, name varchar(255) NOT NULL, created_at timestamptz NOT NULL
);
CREATE TABLE app_user (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, email varchar(255) NOT NULL UNIQUE,
  password_hash varchar(255) NOT NULL, display_name varchar(255) NOT NULL,
  role varchar(30) NOT NULL, enabled boolean NOT NULL DEFAULT true
);
CREATE INDEX idx_user_org ON app_user(organization_id);
CREATE TABLE site (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, code varchar(255) NOT NULL, name varchar(255) NOT NULL,
  address varchar(255), city varchar(255), postal_code varchar(255),
  contact_name varchar(255), contact_email varchar(255),
  CONSTRAINT uq_site_code UNIQUE (organization_id, code)
);
CREATE INDEX idx_site_org ON site(organization_id);
CREATE TABLE asset (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, site_id uuid NOT NULL REFERENCES site(id),
  code varchar(255) NOT NULL, name varchar(255) NOT NULL, type varchar(30) NOT NULL,
  location varchar(255), manufacturer varchar(255), serial_number varchar(255),
  status varchar(30) NOT NULL, installed_on date,
  CONSTRAINT uq_asset_code UNIQUE (organization_id, code)
);
CREATE INDEX idx_asset_site ON asset(organization_id, site_id);
CREATE TABLE maintenance_plan (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, asset_id uuid NOT NULL REFERENCES asset(id),
  title varchar(255) NOT NULL, interval_days integer NOT NULL CHECK (interval_days > 0),
  next_due_date date NOT NULL, instructions varchar(255),
  assigned_to_user_id uuid REFERENCES app_user(id), active boolean NOT NULL DEFAULT true
);
CREATE INDEX idx_plan_due ON maintenance_plan(active, next_due_date);
CREATE TABLE work_order (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, site_id uuid NOT NULL REFERENCES site(id),
  asset_id uuid REFERENCES asset(id), maintenance_plan_id uuid REFERENCES maintenance_plan(id),
  scheduled_for date, number varchar(255) NOT NULL, title varchar(255) NOT NULL,
  description varchar(2000), status varchar(30) NOT NULL, priority varchar(30) NOT NULL,
  assigned_to_user_id uuid REFERENCES app_user(id), due_date date,
  resolution varchar(2000), started_at timestamptz, completed_at timestamptz,
  approved_at timestamptz, updated_at timestamptz,
  created_by_user_id uuid REFERENCES app_user(id),
  CONSTRAINT uq_work_number UNIQUE(organization_id, number),
  CONSTRAINT uq_work_plan_cycle UNIQUE(maintenance_plan_id, scheduled_for)
);
CREATE INDEX idx_work_org_status ON work_order(organization_id,status);
CREATE INDEX idx_work_org_due ON work_order(organization_id,due_date);
CREATE TABLE inspection_template (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, name varchar(255) NOT NULL,
  description varchar(255), asset_type varchar(30), active boolean NOT NULL DEFAULT true
);
CREATE TABLE checklist_item (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, template_id uuid NOT NULL REFERENCES inspection_template(id),
  position integer NOT NULL, label varchar(255) NOT NULL, required boolean NOT NULL DEFAULT true,
  CONSTRAINT uq_checklist_pos UNIQUE (template_id, position)
);
CREATE TABLE inspection (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, template_id uuid NOT NULL REFERENCES inspection_template(id),
  asset_id uuid NOT NULL REFERENCES asset(id), inspector_user_id uuid NOT NULL REFERENCES app_user(id),
  performed_at timestamptz NOT NULL, status varchar(30) NOT NULL,
  notes varchar(255), number varchar(255) NOT NULL,
  CONSTRAINT uq_inspection_number UNIQUE (organization_id,number)
);
CREATE INDEX idx_inspection_asset ON inspection(organization_id,asset_id);
CREATE TABLE inspection_result (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, inspection_id uuid NOT NULL REFERENCES inspection(id),
  checklist_item_id uuid NOT NULL REFERENCES checklist_item(id),
  label_snapshot varchar(255) NOT NULL, position integer NOT NULL,
  passed boolean NOT NULL, note varchar(255),
  CONSTRAINT uq_inspection_item UNIQUE (inspection_id, checklist_item_id)
);
CREATE TABLE defect (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, asset_id uuid NOT NULL REFERENCES asset(id),
  inspection_id uuid NOT NULL REFERENCES inspection(id),
  corrective_work_order_id uuid REFERENCES work_order(id), title varchar(255) NOT NULL,
  details varchar(255), severity varchar(30) NOT NULL, status varchar(30) NOT NULL,
  resolved_at timestamptz
);
CREATE INDEX idx_defect_org_status ON defect(organization_id,status);
CREATE TABLE audit_event (
  id uuid PRIMARY KEY, organization_id uuid NOT NULL REFERENCES organization(id),
  created_at timestamptz NOT NULL, actor_user_id uuid REFERENCES app_user(id),
  entity_type varchar(255) NOT NULL, entity_id uuid NOT NULL,
  action varchar(255) NOT NULL, description varchar(255)
);
CREATE INDEX idx_audit_org_time ON audit_event(organization_id,created_at DESC);
