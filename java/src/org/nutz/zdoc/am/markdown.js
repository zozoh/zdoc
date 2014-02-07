var ioc = {
	am : {
		fields : {
			name : {refer: '$Name'},
		}
	},
	// 段落自动机
	mdParagraph : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocParallelAm',
		fields : {
			ams : [{
				refer:'mdImg'
			},{
				refer:'mdImgById'
			},{
				refer:'mdLink'
			},{
				refer:'mdLinkById'
			},{
				refer:'mdQuote'
			},{
				refer:'mdEm'
			},{
				refer:'mdEm2'
			},{
				refer:'mdEm_'
			},{
				refer:'mdEm_2'
			}]
		}
	},
	// 图片
	mdImg : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			theChar : '!',
			ams : [{
				type : 'org.nutz.zdoc.am.OneCharAm',
				args : ['[']
			},{
				type : 'org.nutz.zdoc.am.MdLinkTextAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkSepAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkInfoAm',
				args : ['src']
			},{
				type : 'org.nutz.zdoc.am.MdImgDoneAm'
			}]
		}
	},
	mdImgById : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			theChar : '!',
			ams : [{
				type : 'org.nutz.zdoc.am.OneCharAm',
				args : ['[']
			},{
				type : 'org.nutz.zdoc.am.MdLinkTextAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkSepAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkIdAm',
				args : ['src']
			},{
				type : 'org.nutz.zdoc.am.MdImgDoneAm'
			}]
		}
	},
	// 链接
	mdLink : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			theChar : '[',
			ams : [{
				type : 'org.nutz.zdoc.am.MdLinkTextAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkSepAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkInfoAm',
				args : ['href']
			},{
				type : 'org.nutz.zdoc.am.MdLinkDoneAm'
			}]
		}
	},
	mdLinkById : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			theChar : '[',
			ams : [{
				type : 'org.nutz.zdoc.am.MdLinkTextAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkSepAm'
			},{
				type : 'org.nutz.zdoc.am.MdLinkIdAm',
				args : ['href']
			},{
				type : 'org.nutz.zdoc.am.MdLinkDoneAm'
			}]
		}
	},
	// 普通文字
	mdText : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.TextAm',
		args   : ['!<[`*_']
	},
	// 反引号
	mdQuote : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocQuoteAm',
		args   : ['`']
	},
	// 重点文字
	mdEm : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.MdEmphasisAm',
		args   : ["*"]
	},mdEm2 : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.MdEmphasisAm',
		args   : ["**"]
	},mdEm_ : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.MdEmphasisAm',
		args   : ["_"]
	},mdEm_2 : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.MdEmphasisAm',
		args   : ["__"]
	},
	
}