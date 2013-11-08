var ioc = {
	// 段落自动机
	zdocParagraph : {
		type : 'org.nutz.zdoc.am.ZDocParallelAm',
		fields : {
			name : {refer: '$Name'},
			ams : [{
				refer:'zdocImg'
			},{
				refer:'zdocLink'
			},{
				refer:'zdocText'
			}]
		}
	},
	// 文字
	zdocText : {
		type   : 'org.nutz.zdoc.am.TextAm',
		args   : ['<[{'],
		fields : {
			name : {refer: '$Name'}
		} 
	},
	// 链接
	zdocLink : {
		type : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			name    : {refer: '$Name'},
			theChar : '[',
			ams : [{
				type : 'org.nutz.zdoc.am.ZDocLinkHrefAm'
			},{
				refer:'zdocLinkContent'
			}]
		}
	},
	zdocLinkContent : {
		type : 'org.nutz.zdoc.am.ZDocParallelAm',
		fields : {
			name : {refer: '$Name'},
			theChar : ']',
			ams : [{
				refer:'zdocImg'
			},{
				refer:'zdocText'
			}]
		}
	},
	// 图片
	zdocImg : {
		type : 'org.nutz.zdoc.am.ZDocSeriesAm',
		fields : {
			name : {refer: '$Name'},
			theChar : '<',
			ams : [{
				type : 'org.nutz.zdoc.am.ZDocImgSrcAm'
			},{
				type : 'org.nutz.zdoc.am.ZDocImgAltAm'
			}]
		}
	}
}