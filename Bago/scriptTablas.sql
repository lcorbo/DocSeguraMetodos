SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPADescargas]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPADescargas](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[dosExtension] [varchar](5) NOT NULL,
 CONSTRAINT [PK_TP_LPADescargas] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAWorkflow_Mail]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAWorkflow_Mail](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombrePaso] [varchar](64) NOT NULL,
	[subject] [varchar](128) NOT NULL,
	[body] [varchar](256) NOT NULL,
	[destinatarios] [varchar](10) NOT NULL,
	[propiedadBPM] [varchar](64) NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAControl_Impresion]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAControl_Impresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[codigo] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
	[cantCopias] [int] NOT NULL,
	[tipoCopia] [int] NOT NULL,
	[permitir] [varchar](2) NOT NULL,
 CONSTRAINT [PK_TD_LPAControl_Impresion] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAServiceImpresion]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAServiceImpresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
	[idImpresion] [int] NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[impresora] [int] NOT NULL,
	[tipoCopia] [int] NOT NULL,
	[procesada] [varchar](2) NOT NULL CONSTRAINT [DF_TD_LPAServiceImpresion_realizada]  DEFAULT ('NO'),
	[fechaImpresion] [datetime] NULL,
	[mensajeError] [varchar](256) NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAOpenDTS]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAOpenDTS](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[MarcaAgua] [varchar](2) NOT NULL,
	[texto] [varchar](128) NOT NULL,
 CONSTRAINT [PK_TP_LPAOpenDTS] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAViewer_BuscadorAnexos]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAViewer_BuscadorAnexos](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](256) NOT NULL,
	[descripcion] [varchar](256) NOT NULL,
	[propiedades] [varchar](1024) NOT NULL,
	[nombrePropiedades] [varchar](2048) NOT NULL,
	[tipoPropiedades] [varchar](1024) NOT NULL,
	[resultados] [varchar](1024) NOT NULL,
	[nombreResultados] [varchar](2048) NOT NULL,
	[tipoResultados] [varchar](1024) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAWorkflow_Tasks]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAWorkflow_Tasks](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombreWorkflow] [varchar](128) NOT NULL,
	[nombrePaso] [varchar](128) NOT NULL,
	[propiedadUsuarios] [varchar](128) NOT NULL,
	[documento] [varchar](6) NOT NULL,
 CONSTRAINT [PK_TP_LPAWorkflow_Tasks] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAViewer_Botones_Grupo]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAViewer_Botones_Grupo](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
	[botones] [varchar](32) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAOrdenes]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAOrdenes](
	[idOrden] [int] IDENTITY(1,1) NOT NULL,
	[nombreWorkflow] [varchar](128) NOT NULL,
	[idDocumento] [varchar](16) NULL,
	[precargado] [varchar](2) NOT NULL,
	[ejecutado] [varchar](2) NOT NULL,
	[valoresPropiedadesBPM] [varchar](256) NULL,
	[fechaEjecucion] [datetime] NULL,
 CONSTRAINT [PK_TD_LPAOrdenes] PRIMARY KEY CLUSTERED 
(
	[idOrden] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPABOF_back]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPABOF_back](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[lifeCycle] [varchar](128) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[propiedadCodigo] [varchar](64) NULL,
	[propiedadGrupos] [varchar](64) NULL,
	[propiedadSector] [varchar](64) NULL,
	[propiedadesUsuarios] [varchar](128) NULL,
	[gruposExtra] [varchar](128) NULL,
	[gruposFueraVigencia] [varchar](128) NULL,
	[propiedadReemplazos] [varchar](64) NULL,
	[propiedadVersion] [varchar](64) NULL,
	[estadoVigente] [varchar](64) NULL,
	[estadoInicial] [varchar](64) NULL,
	[propiedadCopia] [varchar](64) NULL,
 CONSTRAINT [PK_TP_LPABOF] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAWorkflow_Hist]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAWorkflow_Hist](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[idWorkflow] [varchar](32) NOT NULL,
	[nombreWorkflow] [varchar](64) NOT NULL,
	[mensaje] [varchar](256) NOT NULL,
	[fecha] [datetime] NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[documentoAdjunto] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
 CONSTRAINT [PK_TD_LPAWorkflow_Hist] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAViewer_BuscadorRelaciones]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAViewer_BuscadorRelaciones](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](256) NOT NULL,
	[descripcion] [varchar](256) NOT NULL,
	[propiedades] [varchar](1024) NOT NULL,
	[nombrePropiedades] [varchar](2048) NOT NULL,
	[tipoPropiedades] [varchar](1024) NOT NULL,
	[resultados] [varchar](1024) NOT NULL,
	[nombreResultados] [varchar](2048) NOT NULL,
	[tipoResultados] [varchar](1024) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAVencimientos]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAVencimientos](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[propiedadVigencia] [varchar](128) NOT NULL,
	[propiedadVencimiento] [varchar](128) NOT NULL,
	[diasVencimiento] [int] NOT NULL,
	[propiedadNotificado] [varchar](128) NOT NULL,
	[diasNotificacion] [int] NOT NULL,
	[diasPreVencimiento] [int] NOT NULL,
	[workflowVencimiento] [varchar](128) NOT NULL,
	[estadoVigente] [varchar](32) NOT NULL,
 CONSTRAINT [PK_TP_LPAVencimientos] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAControl_Impresion]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAControl_Impresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[sector] [varchar](64) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAWorkflow_Doc]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAWorkflow_Doc](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[bookmark] [varchar](64) NOT NULL,
	[propiedad] [varchar](64) NOT NULL,
	[tipo] [varchar](32) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAWorkflow_Hist]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAWorkflow_Hist](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombrePaso] [varchar](64) NOT NULL,
	[mensajeLog] [varchar](256) NOT NULL,
	[pasoAnterior] [varchar](64) NULL,
 CONSTRAINT [PK_TP_LPAWorkflow_Hist] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TV_LPAImpresoras]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TV_LPAImpresoras](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombreImpresora] [varchar](128) NOT NULL,
	[grupo] [varchar](64) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPARelaciones]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPARelaciones](
	[idrelacion] [int] IDENTITY(1,1) NOT NULL,
	[idOrigen] [varchar](16) NOT NULL,
	[idDestino] [varchar](16) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fechaRelacion] [datetime] NOT NULL,
	[codigoDocOrigen] [varchar](64) NOT NULL,
	[versionDocOrigen] [varchar](8) NOT NULL,
	[codigoDocDestino] [varchar](64) NOT NULL,
	[versionDocDestino] [varchar](8) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAAnexos]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAAnexos](
	[idrelacion] [int] IDENTITY(1,1) NOT NULL,
	[idOrigen] [varchar](16) NOT NULL,
	[idDestino] [varchar](16) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fechaRelacion] [datetime] NOT NULL,
	[codigoDocOrigen] [varchar](64) NOT NULL,
	[versionDocOrigen] [varchar](8) NOT NULL,
	[codigoDocDestino] [varchar](64) NOT NULL,
	[versionDocDestino] [varchar](8) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAHistorial_Documento]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAHistorial_Documento](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[codigo] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[evento] [varchar](64) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fecha] [datetime] NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPANomenclador]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPANomenclador](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](256) NOT NULL,
	[propiedadCodigo] [varchar](64) NOT NULL,
	[propiedadNumerador] [varchar](64) NULL,
	[numerador] [varchar](2) NOT NULL CONSTRAINT [DF_TP_LPANomenclador_numerador]  DEFAULT ('NO'),
	[propiedades] [varchar](1024) NOT NULL,
	[tipoPropiedades] [varchar](1024) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAWorkflow_BPM]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAWorkflow_BPM](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[packageAdjunto] [varchar](64) NOT NULL,
	[packageBPM] [varchar](64) NULL,
	[claseBPM] [varchar](64) NULL,
	[propiedades] [varchar](256) NULL,
 CONSTRAINT [PK_TP_LPAWorkflow_BPM] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPABOF]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPABOF](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[lifeCycle] [varchar](128) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[propiedadCodigo] [varchar](64) NULL,
	[propiedadGrupos] [varchar](64) NULL,
	[propiedadSector] [varchar](64) NULL,
	[propiedadesUsuarios] [varchar](128) NULL,
	[gruposExtra] [varchar](128) NULL,
	[gruposFueraVigencia] [varchar](128) NULL,
	[propiedadReemplazos] [varchar](64) NULL,
	[propiedadVersion] [varchar](64) NULL,
	[estadoVigente] [varchar](64) NULL,
	[estadoInicial] [varchar](64) NULL,
	[propiedadCopia] [varchar](64) NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPANomenclador]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPANomenclador](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](128) NOT NULL,
	[p_sector] [varchar](128) NULL,
	[p_tipodoc] [varchar](128) NULL,
	[numerador] [int] NOT NULL CONSTRAINT [DF_TD_LPANomenclador_numerador]  DEFAULT ((0))
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAHistorial_Impresion]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAHistorial_Impresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fecha] [datetime] NOT NULL,
	[tipoCopia] [int] NOT NULL,
	[idImpresora] [int] NOT NULL,
	[comentario] [varchar](256) NULL,
	[codigoDoc] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPATipoCopias]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPATipoCopias](
	[ID] [int] NOT NULL,
	[tipoCopia] [varchar](64) NOT NULL,
	[controlImpresion] [varchar](2) NOT NULL,
	[comboDesplegable] [varchar](2) NOT NULL,
	[leyendaWord] [varchar](128) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TD_LPAWorkflow_Comments]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TD_LPAWorkflow_Comments](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombreWorkflow] [varchar](128) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
	[codDocumento] [varchar](32) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[idComentario] [varchar](16) NOT NULL
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAGrupos_SinControl]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAGrupos_SinControl](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TP_LPAViewer_Botones]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TP_LPAViewer_Botones](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
	[botones] [varchar](32) NOT NULL,
 CONSTRAINT [PK_TP_LPAViewer_Botones] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[TV_LPABotones]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[TV_LPABotones](
	[ID] [int] NOT NULL,
	[NombreBoton] [varchar](64) NOT NULL,
	[Descripcion] [varchar](128) NOT NULL
) ON [PRIMARY]
END
