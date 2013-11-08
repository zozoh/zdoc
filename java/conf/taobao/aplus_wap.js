(function() {
	var U = window, f = document, X = "g_tb_aplus_loaded";
	if (!f.getElementsByTagName("body").length) {
		setTimeout(arguments.callee, 50);
		return
	}
	if (U[X]) {
		return
	}
	U[X] = 1;
	var G = "http://a.tbcdn.cn/s/fdc/lsproxy.js?v=20130106";
	var x = "0", j = location, F = "https:" == j.protocol, p = parent !== self, P = j.pathname, ah = j.hostname, r = (F
			? "https://"
			: "http://") + "log.mmstat.com/", C = r + "m.gif", N = [ [
		"logtype",
		p ? 0 : 1 ] ], M = location.href, S = f.referrer, af = F && (M
			.indexOf("login.m.taobao.com") >= 0 || M
			.indexOf("login.m.tmall.com") >= 0), H = !!f.attachEvent, ad = "attachEvent", n = "addEventListener", b = H
			? ad
			: n, c = false, I = true, D = "::-plain-::", Z, y, k, t = e("cna"), ae = {}, R = {}, ab, m;
	S = (function() {
		var aq, ap = "wm_referrer", ao = "refer_pv_id", an = U.name || "", al = v(an), ar = al[ap], am = al.wm_old_value;
		aq = f.referrer || T(ar);
		Z = al[ao];
		if (!af) {
			if (!o(am)) {
				U.name = T(am)
			} else {
				if (!o(ar)) {
					U.name = an.replace(/&?\bwm_referrer=[^&]*/g, "")
				}
			}
		}
		return aq
	})();
	function d(am, al) {
		return am.indexOf(al) == 0
	}
	function ac(ao, an) {
		var am = ao.length, al = an.length;
		return am >= al && ao.indexOf(an) == (am - al)
	}
	function a(al) {
		return l(al) ? al.replace(/^\s+|\s+$/g, "") : ""
	}
	function T(ao, am) {
		var al = am || "";
		if (ao) {
			try {
				al = decodeURIComponent(ao)
			} catch (an) {
			}
		}
		return al
	}
	function h(ao) {
		var al = [], an, am;
		for (an in ao) {
			if (ao.hasOwnProperty(an)) {
				am = "" + ao[an];
				al.push(d(an, D) ? am : (an + "=" + encodeURIComponent(am)))
			}
		}
		return al.join("&")
	}
	function E(am) {
		var an = [], ap, ao, aq, al = am.length;
		for (aq = 0; aq < al; aq++) {
			ap = am[aq][0];
			ao = am[aq][1];
			an.push(d(ap, D) ? ao : (ap + "=" + encodeURIComponent(ao)))
		}
		return an.join("&")
	}
	function u(an, am) {
		for ( var al in am) {
			if (am.hasOwnProperty(al)) {
				an[al] = am[al]
			}
		}
		return an
	}
	function v(aq) {
		var am = aq.split("&"), an = 0, al = am.length, ao, ap = {};
		for (; an < al; an++) {
			ao = am[an].split("=");
			ap[ao[0]] = T(ao[1])
		}
		return ap
	}
	function ag(al) {
		return typeof al == "number"
	}
	function o(al) {
		return typeof al == "undefined"
	}
	function l(al) {
		return typeof al == "string"
	}
	function B(al) {
		return Object.prototype.toString.call(al) === "[object Array]"
	}
	function A(al, am) {
		return al && al.getAttribute ? (al.getAttribute(am) || "") : ""
	}
	function Q(am) {
		var al;
		try {
			al = a(am.getAttribute("href", 2))
		} catch (an) {
		}
		return al || ""
	}
	function L() {
		var al = f.getElementById("tb-beacon-aplus");
		return A(al, "exparams")
	}
	function w() {
		y = y || f.getElementsByTagName("head")[0];
		return k || (y ? (k = y.getElementsByTagName("meta")) : [])
	}
	function Y(aq, ar) {
		var an = aq.split(";"), ao, am = an.length, al, ap;
		for (ao = 0; ao < am; ao++) {
			al = an[ao].split("=");
			ap = a(al[0]);
			if (ap) {
				ar[ap] = T(a(al[1]))
			}
		}
	}
	function e(al) {
		var am = f.cookie.match(new RegExp("\\b" + al + "=([^;]+)"));
		return am ? am[1] : ""
	}
	function J() {
		return Math.floor(Math.random() * 268435456).toString(16)
	}
	function i() {
		var am, ap, an = w(), al = an.length, ao;
		for (am = 0; am < al; am++) {
			ap = an[am];
			if (A(ap, "name") == "atp-beacon") {
				ao = A(ap, "content");
				Y(ao, R)
			}
		}
		ab = h(R)
	}
	function O(am) {
		am = (am || "").split("#")[0].split("?")[0];
		var al = am.length, an = function(ar) {
			var aq, ao = ar.length, ap = 0;
			for (aq = 0; aq < ao; aq++) {
				ap = ap * 31 + ar.charCodeAt(aq)
			}
			return ap
		};
		return al ? an(al + "#" + am.charCodeAt(al - 1)) : -1
	}
	function g() {
		if (p) {
			return
		}
		var am = U.name || "", an = af ? (f.referrer || M) : M, al = {};
		if (F) {
			al.wm_referrer = an
		}
		if (am.indexOf("=") == -1) {
			al.wm_old_value = am;
			U.name = h(al)
		} else {
			if (af && am.match(/&?\bwm_referrer=[^&]+/)) {
				delete al.wm_referrer
			}
			am = v(am);
			u(am, al);
			U.name = h(am)
		}
	}
	function ak(am, an, al) {
		am[b]((H ? "on" : "") + an, function(ap) {
			ap = ap || U.event;
			var ao = ap.target || ap.srcElement;
			al(ap, ao)
		}, c)
	}
	function K() {
		var am, al, ao = [];
		for (am = 0, al = ao.length; am < al; am++) {
			if (P.indexOf(ao[am]) != -1) {
				return I
			}
		}
		var an = /^https?:\/\/[\w\.]+\.(taobao|tmall|etao|tao123|juhuasuan)\.com/i;
		return !an.test(S)
	}
	function aa(al, am) {
		if (!am) {
			return
		}
		if (!ai()) {
			return m.send(al, am)
		} else {
			return W({
				url : s(al, am),
				js : G
			})
		}
	}
	function q() {
		return D + Math.random()
	}
	function aj(an, al) {
		var am = f.createElement("script");
		am.type = "text/javascript";
		am.async = true;
		am.src = F ? al : an;
		f.getElementsByTagName("head")[0].appendChild(am)
	}
	function z(ao, am) {
		var an = document.createElement("iframe");
		an.style.width = "1px";
		an.style.height = "1px";
		an.style.position = "absolute";
		an.style.display = "none";
		an.src = ao;
		if (am) {
			an.name = am
		}
		var al = document.getElementsByTagName("body")[0];
		al.appendChild(an);
		return an
	}
	function ai() {
		if (F) {
			return false
		}
		var am = navigator.userAgent;
		var al = am.split(" Safari/");
		if (al.length != 2) {
			return false
		}
		return U.localStorage && U.postMessage
				&& al[1].match(/[\d\.]+/)
				&& am.indexOf("AppleWebKit") > -1
				&& am.match(/\bVersion\/\d+/)
				&& !am.match(/\bChrome\/\d+/)
	}
	function W(al) {
		var am = "http://cdn.mmstat.com/aplus-proxy.html?v=20130115";
		z(am, JSON.stringify(al));
		if (U.addEventListener && U.JSON) {
			U.addEventListener("message", function(an) {
				var ar = an.data;
				function av() {
					var ay = ah.split(".");
					var ax = ay.length;
					if (ax > 1) {
						return ay[ax - 2] + "." + ay[ax - 1]
					} else {
						return ah
					}
				}
				try {
					ar = JSON.parse(ar)
				} catch (au) {
					return
				}
				var aw, ao, aq;
				for ( var at = 0, ap = ar.length; at < ap; at++) {
					aw = ar[at];
					aq = aw.k;
					ao = encodeURIComponent(aq) + "="
							+ (aq == "cna" ? aw.v : encodeURIComponent(aw.v))
							+ "; domain=."
							+ av()
							+ "; path=/; expires="
							+ (new Date(aw.t)).toGMTString();
					f.cookie = ao
				}
			})
		}
	}
	function s(am, ao) {
		var an = am.indexOf("?") == -1 ? "?" : "&", al = ao ? (B(ao)
				? E(ao)
				: h(ao)) : "";
		return al ? (am + an + al) : am
	}
	function V() {
		var ao = "http://g.tbcdn.cn/tb/fdc/??spm_wap.js,spmact.js?v=131015";
		var am = [ "itb.m.taobao.com/", "re.m.taobao.com/" ];
		var an, al = am.length;
		for (an = 0; an < al; an++) {
			if (M.indexOf(am[an]) > -1) {
				aj(ao, ao);
				break
			}
		}
	}
	m = {
		version : x,
		referrer : S,
		_d : {},
		_microscope_data : ae,
		getCookie : e,
		tryToGetAttribute : A,
		tryToGetHref : Q,
		isNumber : ag,
		send : function(an, ap) {
			var am = new Image(), ar = "_img_" + Math.random(), ao = an
					.indexOf("?") == -1 ? "?" : "&", aq, al = ap ? (B(ap)
					? E(ap)
					: h(ap)) : "";
			U[ar] = am;
			am.onload = am.onerror = function() {
				U[ar] = null
			};
			am.src = aq = al ? (an + ao + al) : an;
			am = null;
			return aq
		},
		record : function(am, at, ao, al) {
			al = arguments[3] || "";
			var an, ar = "?", aq = c, ap;
			if (am == "ac") {
				an = "http://ac.atpanel.com/1.gif";
				aq = d(al, "A") && (al.substring(1) == O(at))
			} else {
				if (d(am, "/")) {
					aq = d(al, "H") && (al.substring(1) == O(am));
					an = r + am.substring(1);
					ap = I
				} else {
					if (ac(am, ".gif")) {
						an = r + am
					} else {
						return c
					}
				}
			}
			if (!aq && al != "%" && O(M) != al) {
				return c
			}
			an += ar + "cache="
					+ J()
					+ "&gmkey="
					+ encodeURIComponent(at)
					+ "&gokey="
					+ encodeURIComponent(ao)
					+ "&cna="
					+ t
					+ "&isbeta="
					+ x;
			if (ap) {
				an += "&logtype=2"
			}
			if (!ai()) {
				return m.send(an)
			} else {
				return W({
					url : an,
					js : G
				})
			}
		}
	};
	U.goldlog = m;
	V();
	(function() {
		var ap, ao = e("tracknick");
		if (!p || K()) {
			ap = [
				[ q(), "title=" + escape(f.title) ],
				[ "pre", S ],
				[ "cache", J() ],
				[ "scr", screen.width + "x" + screen.height ],
				[ "isbeta", x ] ];
			if (t) {
				ap.push([ q(), "cna=" + t ])
			}
			if (ao) {
				ap.push([ q(), "nick=" + ao ])
			}
			N = N.concat(ap);
			N.push([ q(), L() ]);
			U.g_aplus_pv_req = aa(C, N)
		}
		if (p) {
			i();
			var am, al = R.on, an = (al == "1"
					? "http://ac.atpanel.com/y.gif"
					: C);
			if ((al == "1" || al == "2") && (am = R.chksum)
					&& am === O(M).toString())
			{
				aa(an, N)
			}
		}
		if (af) {
			g()
		} else {
			ak(U, "beforeunload", function() {
				g()
			})
		}
	})()
})();