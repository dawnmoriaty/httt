export default function Home() {
  return (
    <main className="flex min-h-screen flex-col bg-[radial-gradient(circle_at_top,_#e7eef8,_#f8fafc_40%,_#ffffff_100%)] px-6 py-12 text-slate-900">
      <section className="mx-auto flex w-full max-w-6xl flex-1 flex-col gap-10 lg:gap-14">
        <div className="grid gap-8 rounded-[2rem] border border-slate-200/80 bg-white/80 p-8 shadow-[0_20px_80px_rgba(15,23,42,0.08)] backdrop-blur lg:grid-cols-[1.3fr_0.9fr] lg:p-12">
          <div className="space-y-6">
            <span className="inline-flex rounded-full border border-sky-200 bg-sky-50 px-4 py-1 text-sm font-semibold text-sky-700">
              HTTT RBAC Bootstrap
            </span>
            <div className="space-y-4">
              <h1 className="max-w-3xl text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
                Nen tang auth va phan quyen dong de gan vao de tai sau nay.
              </h1>
              <p className="max-w-2xl text-lg leading-8 text-slate-600">
                Backend da co JWT, Redis session, role-permission matrix va sample resource `subscription` de test
                cac quyen `VIEW`, `ADD`, `UPDATE`, `DELETE`, `IMPORT`, `EXPORT`.
              </p>
            </div>
            <div className="flex flex-wrap gap-3 text-sm font-medium text-slate-700">
              <span className="rounded-full bg-slate-100 px-4 py-2">Spring Boot 4</span>
              <span className="rounded-full bg-slate-100 px-4 py-2">PostgreSQL</span>
              <span className="rounded-full bg-slate-100 px-4 py-2">Redis session</span>
              <span className="rounded-full bg-slate-100 px-4 py-2">Next.js standalone</span>
              <span className="rounded-full bg-slate-100 px-4 py-2">Nginx reverse proxy</span>
            </div>
          </div>

          <div className="rounded-[1.5rem] bg-slate-950 p-6 text-slate-100 shadow-[0_20px_50px_rgba(15,23,42,0.25)]">
            <div className="mb-4 text-sm font-semibold uppercase tracking-[0.24em] text-sky-300">
              Seeded Access
            </div>
            <div className="space-y-4 text-sm leading-7">
              <div>
                <div className="font-semibold text-white">Super Admin</div>
                <div>username: `admin`</div>
                <div>password: `admin123`</div>
              </div>
              <div>
                <div className="font-semibold text-white">Health Check</div>
                <div>`GET /api/public/health`</div>
              </div>
              <div>
                <div className="font-semibold text-white">Core APIs</div>
                <div>`POST /api/auth/login`</div>
                <div>`GET /api/admin/roles`</div>
                <div>`GET /api/admin/permissions`</div>
                <div>`GET /api/subscriptions`</div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          <section className="rounded-[1.5rem] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-slate-950">RBAC Flow</h2>
            <p className="mt-3 text-sm leading-7 text-slate-600">
              Login toi JWT, JWT toi Redis session snapshot, sau do
              <code className="mx-1 rounded bg-slate-100 px-1.5 py-0.5 text-[0.85em] text-slate-900">
                @RequirePermission(resource, action)
              </code>
              se quyet dinh role va permission o backend.
            </p>
          </section>
          <section className="rounded-[1.5rem] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-slate-950">Admin Control</h2>
            <p className="mt-3 text-sm leading-7 text-slate-600">
              Admin tong co the tao role, cap permission dong, gan role cho user va invalidate session khi quyen thay
              doi.
            </p>
          </section>
          <section className="rounded-[1.5rem] border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-slate-950">Future Modules</h2>
            <p className="mt-3 text-sm leading-7 text-slate-600">
              Khi co de tai cu the nhu tour, booking hay report, chi can them controller/service moi va seed permission
              moi theo resource-action.
            </p>
          </section>
        </div>
      </section>
    </main>
  );
}
