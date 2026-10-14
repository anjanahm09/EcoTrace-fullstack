
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  Activity, BarChart3, Building2, CheckCircle2, Cloud,
  Database, Leaf, LoaderCircle, Package, Plus, RefreshCw,
  Search, ShieldCheck, X
} from 'lucide-react';
import './index.css';

const API = (
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
).replace(/\/+$/, '');

const USERNAME = import.meta.env.VITE_DEMO_USERNAME || '';
const PASSWORD = import.meta.env.VITE_DEMO_PASSWORD || '';

const CATEGORIES = [
  { scope: 'SCOPE_1', category: 'Stationary combustion' },
  { scope: 'SCOPE_1', category: 'Mobile combustion' },
  { scope: 'SCOPE_1', category: 'Fugitive emissions' },
  { scope: 'SCOPE_1', category: 'Process emissions' },
  { scope: 'SCOPE_2', category: 'Purchased electricity, steam, heating and cooling' },
  { scope: 'SCOPE_3', category: 'Purchased goods and services' },
  { scope: 'SCOPE_3', category: 'Capital goods' },
  { scope: 'SCOPE_3', category: 'Fuel- and energy-related activities' },
  { scope: 'SCOPE_3', category: 'Upstream transportation and distribution' },
  { scope: 'SCOPE_3', category: 'Waste generated in operations' },
  { scope: 'SCOPE_3', category: 'Business travel' },
  { scope: 'SCOPE_3', category: 'Employee commuting' },
  { scope: 'SCOPE_3', category: 'Upstream leased assets' },
  { scope: 'SCOPE_3', category: 'Downstream transportation and distribution' },
  { scope: 'SCOPE_3', category: 'Processing of sold products' },
  { scope: 'SCOPE_3', category: 'Use of sold products' },
  { scope: 'SCOPE_3', category: 'End-of-life treatment of sold products' },
  { scope: 'SCOPE_3', category: 'Downstream leased assets' },
  { scope: 'SCOPE_3', category: 'Franchises' },
  { scope: 'SCOPE_3', category: 'Investments' }
];

const NAV = [
  { id: 'overview', label: 'Overview', icon: BarChart3 },
  { id: 'activities', label: 'Activities', icon: Activity },
  { id: 'organizations', label: 'Organizations', icon: Building2 },
  { id: 'factors', label: 'Emission factors', icon: Cloud },
  { id: 'categories', label: 'Scope categories', icon: Database }
];

function authHeaders() {
  if (!USERNAME || !PASSWORD) {
    throw new Error(
      'API credentials are missing. Configure VITE_DEMO_USERNAME and VITE_DEMO_PASSWORD in frontend/.env.local, then restart Vite.'
    );
  }

  return {
    Accept: 'application/json',
    Authorization: `Basic ${btoa(`${USERNAME}:${PASSWORD}`)}`
  };
}

async function apiRequest(path, options = {}) {
  const headers = {
    ...authHeaders(),
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...options.headers
  };

  let response;

  try {
    response = await fetch(`${API}${path}`, { ...options, headers });
  } catch {
    throw new Error(
      'Cannot connect to the backend. Check that Spring Boot is running on port 8080.'
    );
  }

  if (!response.ok) {
    const bodyText = await response.text().catch(() => '');
    let detail = '';

    try {
      const body = JSON.parse(bodyText);
      detail = body.message || body.error || '';
    } catch {
      detail = bodyText;
    }

    if (response.status === 401) {
      throw new Error(
        '401 Unauthorized: check frontend/.env.local and the backend credentials.'
      );
    }

    if (response.status === 403) {
      throw new Error(
        '403 Forbidden: this account does not have permission for this operation.'
      );
    }

    throw new Error(
      `${response.status} ${response.statusText}${detail ? ` — ${detail}` : ''}`
    );
  }

  if (response.status === 204) return null;

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

function number(value, digits = 2) {
  const n = Number(value);

  return Number.isFinite(n)
    ? n.toLocaleString('en-IN', { maximumFractionDigits: digits })
    : '—';
}

function scopeLabel(scope) {
  return String(scope || 'Unknown').replaceAll('_', ' ');
}

function dateLabel(value) {
  if (!value) return '—';

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? String(value)
    : date.toLocaleDateString('en-IN');
}

function Badge({ children, tone = '' }) {
  return <span className={`badge ${tone}`}>{children}</span>;
}

function Empty({ title, message }) {
  return (
    <div className="empty-state">
      <Package size={30} />
      <strong>{title}</strong>
      <p>{message}</p>
    </div>
  );
}

function Metric({ title, value, unit, icon: Icon, note }) {
  return (
    <article className="metric-card">
      <div className="metric-top">
        <span className="metric-label">{title}</span>
        <Icon size={20} />
      </div>
      <div className="metric-value">{value}</div>
      {unit && <div className="metric-unit">{unit}</div>}
      <p className="metric-note">{note}</p>
    </article>
  );
}

function ActivityTable({ items }) {
  if (!items.length) {
    return (
      <Empty
        title="No activities recorded"
        message="Activities will appear here after valid activity data is saved."
      />
    );
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Activity</th>
            <th>Scope</th>
            <th>Category</th>
            <th>Quantity</th>
            <th>Emissions</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item, index) => (
            <tr key={item.id ?? `${item.activityName}-${index}`}>
              <td>
                <strong>{item.activityName || 'Unnamed activity'}</strong>
                {item.productBatch && (
                  <div className="table-subtext">
                    Batch: {item.productBatch}
                  </div>
                )}
              </td>
              <td><Badge>{scopeLabel(item.scope)}</Badge></td>
              <td>{item.category || '—'}</td>
              <td>{number(item.quantity, 4)} {item.unit || ''}</td>
              <td>
                <strong>{number(item.emissionsKgCo2e)}</strong>
                <div className="table-subtext">kg CO₂e</div>
              </td>
              <td>{dateLabel(item.activityDate)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function App() {
  const [page, setPage] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [organizations, setOrganizations] = useState([]);
  const [factors, setFactors] = useState([]);
  const [activities, setActivities] = useState([]);
  const [categories, setCategories] = useState(CATEGORIES);
  const [search, setSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const loadData = useCallback(async (refresh = false) => {
    setError('');
    setNotice('');

    if (refresh) setRefreshing(true);
    else setLoading(true);

    try {
      const results = await Promise.allSettled([
        apiRequest('/organizations'),
        apiRequest('/factors'),
        apiRequest('/categories')
      ]);

      const [orgs, emissionFactors, categoryResult] = results;
      let organizationList = [];

      if (orgs.status === 'fulfilled') {
        organizationList = Array.isArray(orgs.value) ? orgs.value : [];
        setOrganizations(organizationList);
      }

      if (emissionFactors.status === 'fulfilled') {
        setFactors(
          Array.isArray(emissionFactors.value) ? emissionFactors.value : []
        );
      }

      if (categoryResult.status === 'fulfilled') {
        if (
          Array.isArray(categoryResult.value) &&
          categoryResult.value.length > 0
        ) {
          setCategories(categoryResult.value);
        }
      }

      let activityFailure = null;
      const organizationId = organizationList[0]?.id;

      if (organizationId != null) {
        try {
          const records = await apiRequest(
            `/activities?organizationId=${encodeURIComponent(organizationId)}`
          );
          setActivities(Array.isArray(records) ? records : []);
        } catch (activityError) {
          activityFailure = activityError;
        }
      } else if (orgs.status === 'fulfilled') {
        setActivities([]);
      }

      const failures = results
        .filter(result => result.status === 'rejected')
        .map(result => result.reason?.message || 'Request failed');

      if (activityFailure) {
        failures.push(activityFailure.message || 'Unable to load activities');
      }

      if (failures.length > 0) setError(failures.join(' | '));
      else if (refresh) setNotice('Data refreshed successfully.');
    } catch (loadError) {
      setError(loadError.message || 'Unable to load data.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const totalEmissions = useMemo(
    () => activities.reduce(
      (sum, item) => sum + (Number(item.emissionsKgCo2e) || 0),
      0
    ),
    [activities]
  );

  const scopeTotals = useMemo(() => {
    const totals = { SCOPE_1: 0, SCOPE_2: 0, SCOPE_3: 0 };

    activities.forEach(item => {
      if (Object.hasOwn(totals, item.scope)) {
        totals[item.scope] += Number(item.emissionsKgCo2e) || 0;
      }
    });

    return totals;
  }, [activities]);

  const filteredActivities = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return activities;

    return activities.filter(item =>
      [
        item.activityName, item.category, item.scope,
        item.unit, item.productBatch, item.stage
      ].some(value => String(value || '').toLowerCase().includes(q))
    );
  }, [activities, search]);

  async function saveActivity(event) {
    event.preventDefault();
    setFormError('');
    setSaving(true);

    const form = new FormData(event.currentTarget);
    const factorId = Number(form.get('factorId'));
    const organizationId = Number(form.get('organizationId'));
    const quantity = Number(form.get('quantity'));
    const category = String(form.get('category') || '');

    const factor = factors.find(item => Number(item.id) === factorId);

    if (!factor || !organizationId) {
      setFormError('Select a valid organization and emission factor.');
      setSaving(false);
      return;
    }

    if (factor.illustrative === true) {
      setFormError(
        'This factor is illustrative and cannot be used for official calculations. Add a properly sourced factor first.'
      );
      setSaving(false);
      return;
    }

    if (!Number.isFinite(quantity) || quantity <= 0) {
      setFormError('Quantity must be greater than zero.');
      setSaving(false);
      return;
    }

    const massValue = String(form.get('massTonnes') || '').trim();
    const distanceValue = String(form.get('distanceKm') || '').trim();
    const allocationValue = String(
      form.get('allocationPercentage') || ''
    ).trim();

    const isFreight = /freight|transportation|transport/i.test(category);

    const massTonnes = massValue === '' ? null : Number(massValue);
    const distanceKm = distanceValue === '' ? null : Number(distanceValue);
    const allocationPercentage =
      allocationValue === '' ? null : Number(allocationValue);

    if (isFreight) {
      if (
        massTonnes == null ||
        !Number.isFinite(massTonnes) ||
        massTonnes <= 0 ||
        distanceKm == null ||
        !Number.isFinite(distanceKm) ||
        distanceKm <= 0
      ) {
        setFormError(
          'For freight activities, enter a positive freight mass in tonnes and distance in kilometres.'
        );
        setSaving(false);
        return;
      }
    }

    if (
      allocationPercentage != null &&
      (!Number.isFinite(allocationPercentage) ||
        allocationPercentage <= 0 ||
        allocationPercentage > 100)
    ) {
      setFormError('Allocation percentage must be greater than 0 and at most 100.');
      setSaving(false);
      return;
    }

    const payload = {
      activityName: String(form.get('activityName') || '').trim(),
      scope: String(form.get('scope') || ''),
      category,
      quantity,
      unit: String(form.get('unit') || '').trim(),
      factorId,
      organizationId,
      productBatch: String(form.get('productBatch') || '').trim() || null,
      stage: String(form.get('stage') || '').trim() || null,
      transportLegId: String(form.get('transportLegId') || '').trim() || null,
      allocationBasis: String(form.get('allocationBasis') || '').trim() || null,
      massTonnes,
      distanceKm,
      allocationPercentage,
      dataQuality: String(form.get('dataQuality') || 'ESTIMATED'),
      activityDate: String(form.get('activityDate') || '') || null
    };

    if (!payload.activityName || !payload.category || !payload.unit) {
      setFormError('Complete all required fields.');
      setSaving(false);
      return;
    }

    if (
      payload.allocationPercentage != null &&
      !payload.allocationBasis
    ) {
      setFormError(
        'Enter an allocation basis when using an allocation percentage.'
      );
      setSaving(false);
      return;
    }

    try {
      await apiRequest('/activities', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      setShowForm(false);
      setNotice('Activity saved successfully.');
      await loadData(true);
    } catch (saveError) {
      setFormError(saveError.message || 'Unable to save activity.');
    } finally {
      setSaving(false);
    }
  }

  const title = NAV.find(item => item.id === page)?.label || 'Overview';
  const verifiedFactors = factors.filter(
    factor => factor.illustrative !== true
  );

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark"><Leaf size={24} /></div>
          <div>
            <strong className="brand-name">EcoTrace</strong>
            <div className="brand-subtitle">SUPPLY CHAIN SUSTAINABILITY</div>
          </div>
        </div>

        <p className="workspace-label">WORKSPACE</p>

        <nav className="nav-list">
          {NAV.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              className={`nav-item ${page === id ? 'nav-item-active' : ''}`}
              onClick={() => {
                setPage(id);
                setSearch('');
              }}
            >
              <Icon size={18} />
              <span>{label}</span>
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="profile">
            <div className="avatar">ET</div>
            <div>
              <strong>EcoTrace Admin</strong>
              <div className="table-subtext">Local demo workspace</div>
            </div>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb">Workspace / {title}</div>
          <button
            className="button button-secondary"
            onClick={() => loadData(true)}
            disabled={refreshing}
          >
            {refreshing
              ? <LoaderCircle className="spin" size={16} />
              : <RefreshCw size={16} />}
            Refresh data
          </button>
        </header>

        <div className="content">
          <section className="page-heading">
            <div>
              <div className="eyebrow">
                <Leaf size={14} /> SUPPLY CHAIN SUSTAINABILITY
              </div>
              <h1>{page === 'overview' ? 'Carbon overview' : title}</h1>
              <p>Track emissions from activity data to supply-chain stages.</p>
            </div>

            {page === 'activities' && (
              <button
                className="button button-primary"
                onClick={() => {
                  setFormError('');
                  setShowForm(true);
                }}
              >
                <Plus size={17} /> Add activity
              </button>
            )}
          </section>

          {error && (
            <div className="alert alert-error">
              <strong>API request failed</strong>
              <p>{error}</p>
              <button
                className="button button-secondary"
                onClick={() => loadData(true)}
              >
                Retry
              </button>
            </div>
          )}

          {notice && (
            <div className="alert alert-success">
              <CheckCircle2 size={18} /> {notice}
              <button
                className="icon-button"
                onClick={() => setNotice('')}
                aria-label="Dismiss"
              >
                <X size={16} />
              </button>
            </div>
          )}

          {loading ? (
            <div className="loading-panel">
              <LoaderCircle className="spin" size={26} />
              <strong>Loading EcoTrace…</strong>
              <span>Connecting to the backend</span>
            </div>
          ) : (
            <>
              {page === 'overview' && (
                <>
                  <section className="metric-grid">
                    <Metric
                      title="Recorded emissions"
                      value={number(totalEmissions)}
                      unit="kg CO₂e"
                      icon={Cloud}
                      note="Sum of saved activity results"
                    />
                    <Metric
                      title="Activities"
                      value={number(activities.length, 0)}
                      icon={Activity}
                      note="Saved activity records"
                    />
                    <Metric
                      title="Organizations"
                      value={number(organizations.length, 0)}
                      icon={Building2}
                      note="Organizations returned by API"
                    />
                    <Metric
                      title="Sourced factors"
                      value={number(verifiedFactors.length, 0)}
                      icon={ShieldCheck}
                      note="Non-illustrative factors"
                    />
                  </section>

                  <section className="overview-grid">
                    <article className="panel">
                      <h2>Emissions by scope</h2>
                      <p>Calculated from saved activity records.</p>

                      {['SCOPE_1', 'SCOPE_2', 'SCOPE_3'].map(
                        (scope, index) => {
                          const max = Math.max(
                            ...Object.values(scopeTotals),
                            1
                          );

                          return (
                            <div className="scope-row" key={scope}>
                              <div className="scope-row-top">
                                <strong>{scopeLabel(scope)}</strong>
                                <span>{number(scopeTotals[scope])} kg CO₂e</span>
                              </div>
                              <div className="progress-track">
                                <div
                                  className={`progress-fill progress-fill-${index + 1}`}
                                  style={{
                                    width: `${Math.min(
                                      100,
                                      scopeTotals[scope] / max * 100
                                    )}%`
                                  }}
                                />
                              </div>
                            </div>
                          );
                        }
                      )}
                    </article>

                    <article className="panel">
                      <h2>Workspace snapshot</h2>
                      <div className="snapshot-row">
                        <span>Organizations</span>
                        <strong>{organizations.length}</strong>
                      </div>
                      <div className="snapshot-row">
                        <span>Emission factors</span>
                        <strong>{factors.length}</strong>
                      </div>
                      <div className="snapshot-row">
                        <span>Illustrative factors</span>
                        <strong>
                          {factors.filter(factor => factor.illustrative === true).length}
                        </strong>
                      </div>
                      <div className="snapshot-row">
                        <span>Activities</span>
                        <strong>{activities.length}</strong>
                      </div>
                      <div className="info-note">
                        <ShieldCheck size={17} />
                        Demo factors are not verified scientific data.
                      </div>
                    </article>
                  </section>

                  <section className="panel table-panel">
                    <h2>Recent activities</h2>
                    <ActivityTable items={[...activities].slice(-5).reverse()} />
                  </section>
                </>
              )}

              {page === 'activities' && (
                <section className="panel table-panel">
                  <div className="panel-heading">
                    <h2>Activity register</h2>
                    <div className="search-box">
                      <Search size={17} />
                      <input
                        value={search}
                        onChange={event => setSearch(event.target.value)}
                        placeholder="Search activities..."
                      />
                    </div>
                  </div>
                  <ActivityTable items={[...filteredActivities].reverse()} />
                </section>
              )}

              {page === 'organizations' && (
                <section className="panel table-panel">
                  <h2>Registered organizations</h2>

                  {organizations.length === 0 ? (
                    <Empty
                      title="No organizations"
                      message="No organization records were returned."
                    />
                  ) : (
                    <div className="table-wrap">
                      <table>
                        <thead>
                          <tr>
                            <th>ID</th><th>Name</th><th>Type</th>
                            <th>Country</th><th>Boundary</th>
                          </tr>
                        </thead>
                        <tbody>
                          {organizations.map(org => (
                            <tr key={org.id}>
                              <td>#{org.id}</td>
                              <td><strong>{org.name}</strong></td>
                              <td>{org.organizationType || '—'}</td>
                              <td>{org.country || '—'}</td>
                              <td>{org.boundaryDescription || '—'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </section>
              )}

              {page === 'factors' && (
                <section className="panel table-panel">
                  <h2>Emission factor library</h2>
                  <p>
                    Confirm the source, geography, reference year and boundary
                    before use.
                  </p>

                  {factors.length === 0 ? (
                    <Empty
                      title="No factors"
                      message="No factors were returned by the backend."
                    />
                  ) : (
                    <div className="table-wrap">
                      <table>
                        <thead>
                          <tr>
                            <th>Name</th><th>Category</th><th>Factor</th>
                            <th>Source</th><th>Year</th><th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {factors.map(factor => (
                            <tr key={factor.id}>
                              <td><strong>{factor.name}</strong></td>
                              <td>{factor.category || '—'}</td>
                              <td>
                                {number(factor.kgCo2ePerUnit, 6)} kg CO₂e /
                                {' '}{factor.activityUnit || 'unit'}
                              </td>
                              <td>
                                {factor.sourceUrl ? (
                                  <a
                                    href={factor.sourceUrl}
                                    target="_blank"
                                    rel="noreferrer"
                                  >
                                    {factor.sourceName || 'Source'}
                                  </a>
                                ) : (
                                  factor.sourceName || 'Not provided'
                                )}
                              </td>
                              <td>{factor.referenceYear || '—'}</td>
                              <td>
                                {factor.illustrative === true ? (
                                  <Badge tone="amber">Illustrative</Badge>
                                ) : (
                                  <Badge tone="green">Non-illustrative</Badge>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </section>
              )}

              {page === 'categories' && (
                <section className="panel table-panel">
                  <h2>Emission scope categories</h2>

                  {['SCOPE_1', 'SCOPE_2', 'SCOPE_3'].map(scope => {
                    const rows = categories.filter(
                      category => String(category.scope).toUpperCase() === scope
                    );

                    return (
                      <section className="category-section" key={scope}>
                        <div className="category-heading">
                          <strong>{scopeLabel(scope)}</strong>
                          <span>{rows.length} categories</span>
                        </div>
                        <div className="category-grid">
                          {rows.map((row, index) => (
                            <div
                              className="category-card"
                              key={`${scope}-${row.category}-${index}`}
                            >
                              <span className="category-number">
                                {String(index + 1).padStart(2, '0')}
                              </span>
                              <span>{row.category}</span>
                            </div>
                          ))}
                        </div>
                      </section>
                    );
                  })}
                </section>
              )}
            </>
          )}

          <footer className="footer">
            © {new Date().getFullYear()} EcoTrace · Local development
          </footer>
        </div>
      </main>

      {showForm && (
        <div className="modal-backdrop">
          <section className="modal" role="dialog" aria-modal="true">
            <div className="modal-heading">
              <div>
                <h2>Add carbon activity</h2>
                <p>
                  Activity data is submitted to the backend for calculation.
                </p>
              </div>
              <button
                className="icon-button"
                onClick={() => setShowForm(false)}
                aria-label="Close"
              >
                <X size={19} />
              </button>
            </div>

            {formError && (
              <div className="alert alert-error">{formError}</div>
            )}

            <form className="activity-form" onSubmit={saveActivity}>
              <label>
                Activity name *
                <input
                  name="activityName"
                  required
                  placeholder="Factory electricity"
                />
              </label>

              <div className="form-row">
                <label>
                  Organization *
                  <select name="organizationId" required defaultValue="">
                    <option value="" disabled>Select organization</option>
                    {organizations.map(org => (
                      <option key={org.id} value={org.id}>{org.name}</option>
                    ))}
                  </select>
                </label>

                <label>
                  Scope *
                  <select name="scope" required defaultValue="SCOPE_1">
                    <option value="SCOPE_1">Scope 1</option>
                    <option value="SCOPE_2">Scope 2</option>
                    <option value="SCOPE_3">Scope 3</option>
                  </select>
                </label>
              </div>

              <label>
                Category *
                <select name="category" required defaultValue="">
                  <option value="" disabled>Select category</option>
                  {categories.map((category, index) => (
                    <option
                      key={`${category.scope}-${category.category}-${index}`}
                      value={category.category}
                    >
                      {scopeLabel(category.scope)} — {category.category}
                    </option>
                  ))}
                </select>
              </label>

              <div className="form-row">
                <label>
                  Quantity *
                  <input
                    name="quantity"
                    type="number"
                    min="0.000001"
                    step="any"
                    required
                  />
                </label>
                <label>
                  Activity unit *
                  <input
                    name="unit"
                    required
                    placeholder="Must match factor unit"
                  />
                </label>
              </div>

              <label>
                Emission factor *
                <select name="factorId" required defaultValue="">
                  <option value="" disabled>Select factor</option>
                  {factors.map(factor => (
                    <option
                      key={factor.id}
                      value={factor.id}
                      disabled={factor.illustrative === true}
                    >
                      {factor.name} — {factor.activityUnit}
                      {factor.illustrative === true ? ' (demo only)' : ''}
                    </option>
                  ))}
                </select>
                <span className="field-help">
                  Illustrative factors cannot be used for official calculations.
                </span>
              </label>

              <div className="form-row">
                <label>
                  Product batch
                  <input name="productBatch" placeholder="BATCH-001" />
                </label>
                <label>
                  Supply-chain stage
                  <select name="stage" defaultValue="">
                    <option value="">Not specified</option>
                    {[
                      'Supplier', 'Manufacturer', 'Logistics', 'Distributor',
                      'Wholesaler', 'Retailer', 'Customer use', 'Recycler'
                    ].map(stage => (
                      <option key={stage}>{stage}</option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="form-row">
                <label>
                  Transport leg ID
                  <input name="transportLegId" />
                </label>
                <label>
                  Allocation basis
                  <input
                    name="allocationBasis"
                    placeholder="e.g. product mass"
                  />
                </label>
              </div>

              {/* New fields: required by the freight and allocation calculations */}
              <div className="form-row">
                <label>
                  Freight mass (tonnes)
                  <input
                    name="massTonnes"
                    type="number"
                    min="0.000001"
                    step="any"
                    placeholder="Required for freight"
                  />
                </label>
                <label>
                  Freight distance (km)
                  <input
                    name="distanceKm"
                    type="number"
                    min="0.000001"
                    step="any"
                    placeholder="Required for freight"
                  />
                </label>
              </div>

              <label>
                Allocation percentage (optional)
                <input
                  name="allocationPercentage"
                  type="number"
                  min="0.001"
                  max="100"
                  step="any"
                  placeholder="e.g. 25"
                />
                <span className="field-help">
                  Enter the percentage of emissions attributable to this product
                  or batch. An allocation basis is required when using a percentage.
                </span>
              </label>

              <div className="form-row">
                <label>
                  Data quality
                  <select name="dataQuality" defaultValue="ESTIMATED">
                    <option value="PRIMARY">Primary</option>
                    <option value="SECONDARY">Secondary</option>
                    <option value="ESTIMATED">Estimated</option>
                  </select>
                </label>
                <label>
                  Activity date
                  <input name="activityDate" type="date" />
                </label>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="button button-secondary"
                  onClick={() => setShowForm(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="button button-primary"
                  disabled={saving}
                >
                  {saving
                    ? <LoaderCircle className="spin" size={16} />
                    : <Plus size={16} />}
                  {saving ? 'Saving…' : 'Save activity'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </div>
  );
}

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
