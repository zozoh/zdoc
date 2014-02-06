var ioc = {
	am : {
		fields : {
			name : {refer: '$Name'},
		}
	},
	// 段落自动机
	zdocParagraph : {
		parent : "am",
		type : 'org.nutz.zdoc.am.ZDocParallelAm',
		fields : {
			ams : [{
				refer:'zdocImg'
			},{
				refer:'zdocLink'
			},{
				refer:'zdocText'
			},{
				refer:'zdocQuote'
			},{
				refer:'zdocEm'
			}]
		}
	},
	// 普通文字
	zdocText : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.TextAm',
		args   : ['<[{`']
	},
	// 反引号
	zdocQuote : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocQuoteAm',
		args   : ['`']
	},
	// 重点文字
	zdocEm : {
		parent : "am",
		type   : 'org.nutz.zdoc.am.ZDocEmphasisAm'
	},
	// 链接
	zdocLink : {
		parent : "am",
		type : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			theChar : '[',
			ams : [{
				type : 'org.nutz.zdoc.am.ZDocLinkHrefAm'
			},{
				refer:'zdocLinkContent'
			}]
		}
	},
	zdocLinkContent : {
		parent : "am",
		type : 'org.nutz.zdoc.am.ZDocParallelAm',
		fields : {
			theChar : ']',
			ams : [{
				refer:'zdocImg'
			},{
				refer:'zdocText'
			},{
				refer:'zdocQuote'
			},{
				refer:'zdocEm'
			}]
		}
	},
	// 图片
	zdocImg : {
		parent : "am",
		type : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			theChar : '<',
			ams : [{
				type : 'org.nutz.zdoc.am.ZDocImgSrcAm'
			},{
				type : 'org.nutz.zdoc.am.ZDocImgAltAm'
			}]
		}
	}
}